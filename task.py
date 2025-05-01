import random
from config import TASK_SIZES
import logging

logger = logging.getLogger()

class Task:
    def __init__(self, id, size, tn, gen_time):
        """
        Initialize a Task.

        Args:
            id (int): Unique identifier for the task
            size (float): Task size in bits
            tn (FogNode): Task Node where the task is generated
            gen_time (float): Generation time in seconds
        """
        self.id = id
        self.size = size
        self.tn = tn
        self.gen_time = gen_time

    @staticmethod
    def generate_task(id, tn, current_time):
        """
        Generate a task with random size.

        Args:
            id (int): Task ID
            tn (FogNode): Task Node
            current_time (float): Current simulation time

        Returns:
            Task: Generated task
        """
        size = random.choice(TASK_SIZES) * 8e6  # Convert MB to bits
        return Task(id, size, tn, current_time)

    def divide_into_subtasks(self, targets):
        """
        Divide the task into subtasks for parallel processing, optimizing sizes to minimize max delay.

        Args:
            targets (list): List of FogNode/VM objects (excluding TN)

        Returns:
            list: List of (target, subtask_size, delay) tuples
        """
        from network import FogNode, VM  # Local import to avoid circular dependency

        if not targets:
            delay = (self.size * self.tn.proc_density) / self.tn.cpu_freq
            logger.info(f"Task {self.id}: No targets, assigning to TN {self.tn.id}: size={self.size/8e6:.2f} MB, delay={delay:.4f}s")
            return [(self.tn, self.size, delay)]

        # Remove duplicates and ensure TN is included if fewer than 2 unique targets
        unique_targets = list(dict.fromkeys(targets))  # Preserve order, remove duplicates
        if len(unique_targets) < 2:
            unique_targets.append(self.tn)
        elif self.tn not in unique_targets:
            unique_targets.append(self.tn)

        # Number of subtasks
        m = len(unique_targets)
        logger.info(f"Task {self.id}: Dividing into {m} subtasks for targets: {[f'FogNode {t.id}' if isinstance(t, FogNode) else f'VM {t.id}' if isinstance(t, VM) else f'TN {t.id}' for t in unique_targets]}")

        # Initial equal split with randomization
        subtask_sizes = [self.size / m * random.uniform(0.8, 1.2) for _ in range(m)]
        total_size = sum(subtask_sizes)
        if total_size != self.size:
            scale = self.size / total_size
            subtask_sizes = [size * scale for size in subtask_sizes]

        # Compute delays
        def compute_delay(target, size):
            if target == self.tn:
                delay = (size * self.tn.proc_density) / self.tn.cpu_freq
            else:
                if isinstance(target, FogNode):
                    trans_time = size / min(self.tn.trans_rate, target.trans_rate)
                else:  # VM
                    trans_time = size / target.trans_rate + 0.05
                proc_time = (size * target.proc_density) / target.cpu_freq
                delay = trans_time + proc_time
            return delay

        subtasks = []
        for i, target in enumerate(unique_targets):
            delay = compute_delay(target, subtask_sizes[i])
            subtasks.append((target, subtask_sizes[i], delay))
            target_name = f"FogNode {target.id}" if isinstance(target, FogNode) else f"VM {target.id}" if isinstance(target, VM) else f"TN {target.id}"
            logger.info(f"Initial subtask {i} to {target_name}: size={subtask_sizes[i]/8e6:.2f} MB, delay={delay:.4f}s")

        # Optimize sizes to minimize max delay
        max_iterations = 10
        step_size = self.size * 0.02  # Reduced to 2% for finer adjustments
        for iteration in range(max_iterations):
            delays = [delay for _, _, delay in subtasks]
            max_delay_idx = delays.index(max(delays))
            min_delay_idx = delays.index(min(delays))
            if delays[max_delay_idx] - delays[min_delay_idx] < 0.001:  # Converged
                logger.info(f"Task {self.id}: Optimization converged after {iteration+1} iterations")
                break
            # Shift size from max to min delay target
            shift = min(step_size, subtask_sizes[max_delay_idx] * 0.05)  # Max 5% of subtask
            subtask_sizes[max_delay_idx] -= shift
            subtask_sizes[min_delay_idx] += shift
            # Ensure non-negative sizes
            subtask_sizes[max_delay_idx] = max(subtask_sizes[max_delay_idx], 0)
            # Recompute subtasks
            subtasks = []
            for i, target in enumerate(unique_targets):
                delay = compute_delay(target, subtask_sizes[i])
                subtasks.append((target, subtask_sizes[i], delay))
            target_max = f"FogNode {unique_targets[max_delay_idx].id}" if isinstance(unique_targets[max_delay_idx], FogNode) else f"VM {unique_targets[max_delay_idx].id}" if isinstance(unique_targets[max_delay_idx], VM) else f"TN {unique_targets[max_delay_idx].id}"
            target_min = f"FogNode {unique_targets[min_delay_idx].id}" if isinstance(unique_targets[min_delay_idx], FogNode) else f"VM {unique_targets[min_delay_idx].id}" if isinstance(unique_targets[min_delay_idx], VM) else f"TN {unique_targets[min_delay_idx].id}"
            logger.info(f"Iteration {iteration+1}: max_delay={max(delays):.4f}s, shifted {shift/8e6:.2f} MB from {target_max} to {target_min}")

        # Adjust final sizes to match total
        total_size = sum(size for _, size, _ in subtasks)
        if abs(total_size - self.size) > 1e-6:
            scale = self.size / total_size
            subtasks = [(target, size * scale, compute_delay(target, size * scale)) for target, size, _ in subtasks]

        # Final logging
        for i, (target, size, delay) in enumerate(subtasks):
            target_name = f"FogNode {target.id}" if isinstance(target, FogNode) else f"VM {target.id}" if isinstance(target, VM) else f"TN {target.id}"
            logger.info(f"Final subtask {i} to {target_name}: size={size/8e6:.2f} MB, delay={delay:.4f}s")

        return subtasks