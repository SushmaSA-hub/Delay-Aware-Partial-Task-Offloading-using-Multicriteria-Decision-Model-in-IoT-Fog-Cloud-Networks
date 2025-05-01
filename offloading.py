from network import FogNode, Network, Cloud, VM
from task import Task
from utils import compute_processing_efficiency
from config import COVERAGE_RADIUS
import simpy
import logging

logger = logging.getLogger()

class Offloader:
    def __init__(self, network, env):
        """
        Initialize the Offloader with a network instance and SimPy environment.

        Args:
            network (Network): The fog computing network
            env (simpy.Environment): SimPy environment for scheduling
        """
        self.network = network
        self.env = env
        self.channels = {node: simpy.Resource(self.env, capacity=1) for node in self.network.fog_nodes}

    def update_roles(self):
        """
        Update the roles of all fog nodes in the network.
        """
        for node in self.network.fog_nodes:
            node.update_role()

    def generate_preference_lists(self):
        """
        Generate preference lists for tasks and HNs/VMs.

        Returns:
            tuple: (task_prefs, hn_prefs)
                - task_prefs: Dict mapping Task to list of (HN/VM, delay) tuples
                - hn_prefs: Dict mapping HN/VM to list of (Task, efficiency) tuples
        """
        self.update_roles()
        tns = [node for node in self.network.fog_nodes if node.role == 'TN']
        hns = [node for node in self.network.fog_nodes if node.role == 'HN']
        vms = self.network.cloud.vms
        targets = hns + vms

        task_prefs = {}
        for node in tns:
            for task in node.task_queue:
                prefs = []
                for target in targets:
                    if isinstance(target, FogNode) and self.network.distance(node.position, target.position) > COVERAGE_RADIUS:
                        continue
                    if isinstance(target, FogNode):
                        trans_time = task.size / min(node.trans_rate, target.trans_rate)
                    else:  # VM
                        trans_time = task.size / target.trans_rate + 0.05
                    proc_time = (task.size * target.proc_density) / target.cpu_freq
                    delay = trans_time + proc_time
                    prefs.append((target, delay))
                prefs.sort(key=lambda x: x[1])
                task_prefs[task] = prefs
                logger.info(f"Task {task.id} at {node.id} prefs: {[(t.id if isinstance(t, FogNode) else f'VM {t.id}', d) for t, d in prefs]}")

        hn_prefs = {target: [] for target in targets}
        for task in task_prefs:
            for target in targets:
                if any(t == target for t, _ in task_prefs[task]):
                    efficiency = compute_processing_efficiency(target, task)
                    hn_prefs[target].append((task, efficiency))
        for target in hn_prefs:
            hn_prefs[target].sort(key=lambda x: x[1])
            if hn_prefs[target]:
                logger.info(f"HN/VM {target.id if isinstance(target, FogNode) else f'VM {target.id}'} prefs: {[(t.id, e) for t, e in hn_prefs[target]]}")

        return task_prefs, hn_prefs

    def m2o_matching(self):
        """
        Perform Many-to-One matching using Deferred Acceptance algorithm.

        Returns:
            dict: Mapping of Task to list of assigned HN/VMs
        """
        task_prefs, hn_prefs = self.generate_preference_lists()
        assignments = {task: [] for task in task_prefs}
        target_slots = {target: target.capacity for target in hn_prefs}
        target_assigned = {target: [] for target in hn_prefs}
        unmatched_tasks = set(task_prefs.keys())
        max_retries = len(task_prefs) * 2
        retries = {task: 0 for task in task_prefs}

        while unmatched_tasks and any(retries[task] < max_retries for task in unmatched_tasks):
            task = unmatched_tasks.pop()
            retries[task] += 1
            logger.info(f"Matching Task {task.id}, attempt {retries[task]}")
            prefs = task_prefs.get(task, [])
            if not prefs:
                logger.info(f"Task {task.id}: No preferences, assigning to TN")
                assignments[task].append(task.tn)
                continue
            # Prioritize fog nodes, ensure unique targets
            fog_prefs = [(t, d) for t, d in prefs if isinstance(t, FogNode)]
            selected_prefs = []
            seen_targets = set()
            for t, d in fog_prefs[:2]:
                if t.id not in seen_targets:
                    selected_prefs.append((t, d))
                    seen_targets.add(t.id)
            # Use VM if no fog nodes after 75% retries
            if not selected_prefs and retries[task] >= max_retries * 0.75:
                vm_prefs = [(t, d) for t, d in prefs if isinstance(t, VM)]
                for t, d in vm_prefs[:1]:
                    if t.id not in seen_targets:
                        selected_prefs.append((t, d))
                        seen_targets.add(t.id)
            logger.info(f"Task {task.id}: Selected prefs: {[(t.id if isinstance(t, FogNode) else f'VM {t.id}', d) for t, d in selected_prefs]}")
            assigned = False
            for target, _ in selected_prefs:
                if target_slots[target] > len(target_assigned[target]):
                    target_assigned[target].append(task)
                    assignments[task].append(target)
                    logger.info(f"Assigned Task {task.id} to {target.id if isinstance(target, FogNode) else f'VM {t.id}'}")
                    assigned = True
                else:
                    current_tasks = target_assigned[target]
                    current_prefs = [(t, e) for t, e in hn_prefs[target] if t in current_tasks]
                    if current_prefs:
                        weakest_task, _ = max(current_prefs, key=lambda x: x[1])
                        task_efficiency = next((e for t, e in hn_prefs[target] if t == task), float('inf'))
                        weakest_efficiency = next(e for t, e in hn_prefs[target] if t == weakest_task)
                        if task_efficiency < weakest_efficiency:
                            target_assigned[target].remove(weakest_task)
                            target_assigned[target].append(task)
                            assignments[task].append(target)
                            assignments[weakest_task].remove(target)
                            logger.info(f"Replaced Task {weakest_task.id} with Task {task.id} for {target.id if isinstance(target, FogNode) else f'VM {target.id}'}")
                            unmatched_tasks.add(weakest_task)
                            assigned = True
            if assigned:
                unmatched_tasks.discard(task)
            else:
                unmatched_tasks.add(task)
                if retries[task] >= max_retries:
                    logger.info(f"Task {task.id}: Max retries reached, assigning to TN")
                    assignments[task].append(task.tn)
                else:
                    logger.info(f"Task {task.id}: No assignment, retrying")

        return assignments

    def schedule_subtask(self, task, target, size, delay):
        """
        Schedule a subtask transmission to a target node.

        Args:
            task (Task): The task being offloaded
            target (FogNode/VM): The target node
            size (float): Subtask size in bits
            delay (float): Computed delay for the subtask

        Returns:
            tuple: (target, size, delay)
        """
        target_name = f"FogNode {target.id}" if isinstance(target, FogNode) else f"VM {target.id}" if isinstance(target, VM) else f"TN {target.id}"
        logger.info(f"Scheduling subtask for Task {task.id} to {target_name}: size={size/8e6:.2f} MB")
        if target != task.tn:
            if isinstance(target, FogNode):
                trans_time = size / min(task.tn.trans_rate, target.trans_rate)
                with self.channels[task.tn].request() as req:
                    yield req
                    logger.info(f"Scheduling subtask to FogNode {target.id}: size={size/8e6:.2f} MB, trans_time={trans_time:.4f}s at t={self.env.now:.4f}s")
                    yield self.env.timeout(trans_time)
            elif isinstance(target, VM):
                trans_time = size / target.trans_rate + 0.05
                logger.info(f"Scheduling subtask to VM {target.id}: size={size/8e6:.2f} MB, trans_time={trans_time:.4f}s at t={self.env.now:.4f}s")
                yield self.env.timeout(trans_time)
        else:
            logger.info(f"Scheduling subtask to TN {target.id}: size={size/8e6:.2f} MB, no transmission")
        return (target, size, delay)

    def otos(self, assignments):
        """
        Optimal Task Offloading and Scheduling: Divide tasks and schedule subtasks.

        Args:
            assignments (dict): Mapping of Task to list of assigned HN/VMs

        Returns:
            dict: Mapping of Task to list of (HN/VM or TN, subtask_size, delay) tuples
        """
        results = {}
        for task, targets in assignments.items():
            target_names = [f"FogNode {t.id}" if isinstance(t, FogNode) else f"VM {t.id}" if isinstance(t, VM) else f"TN {t.id}" for t in targets]
            logger.info(f"Processing OTOS for Task {task.id} with targets: {target_names}")
            if not targets:
                delay = (task.size * task.tn.proc_density) / task.tn.cpu_freq
                results[task] = [(task.tn, task.size, delay)]
            else:
                subtasks = task.divide_into_subtasks(targets)
                scheduled_subtasks = []
                for target, size, delay in subtasks:
                    target_name = f"FogNode {target.id}" if isinstance(target, FogNode) else f"VM {target.id}" if isinstance(target, VM) else f"TN {target.id}"
                    logger.info(f"Initial subtask to {target_name}: size={size/8e6:.2f} MB, delay={delay:.4f}s")
                    scheduled_subtask = yield self.env.process(self.schedule_subtask(task, target, size, delay))
                    scheduled_subtasks.append(scheduled_subtask)
                results[task] = scheduled_subtasks
                for target, size, delay in scheduled_subtasks:
                    if target != task.tn:
                        target.assigned_tasks.append(task)
                task.tn.task_queue.remove(task)
        return results