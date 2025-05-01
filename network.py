import random
import math
import logging
from config import AREA_SIZE, COVERAGE_RADIUS, PROCESSING_DENSITY_FN, CPU_FREQ_FN, TRANSMISSION_RATE, PROCESSING_DENSITY_VM, CPU_FREQ_VM

logger = logging.getLogger()

class FogNode:
    def __init__(self, id, position, proc_density, cpu_freq, trans_rate):
        """
        Initialize a Fog Node.

        Args:
            id (int): Unique identifier
            position (tuple): (x, y) coordinates
            proc_density (float): Processing density (cycles/bit)
            cpu_freq (float): CPU frequency (cycles/s)
            trans_rate (float): Transmission rate (bits/s)
        """
        self.id = id
        self.position = position
        self.proc_density = proc_density
        self.cpu_freq = cpu_freq
        self.trans_rate = trans_rate
        self.role = 'HN'  # Default: Helper Node
        self.task_queue = []
        self.assigned_tasks = []
        self.capacity = 2  # Max tasks to process
        self.neighbors = []  # List of neighboring FogNode objects

    def update_role(self):
        """
        Update node role based on task queue.
        """
        self.role = 'TN' if self.task_queue else 'HN'
        logger.info(f"Node {self.id} role updated to {self.role}")

class VM:
    def __init__(self, id, proc_density, cpu_freq, trans_rate):
        """
        Initialize a Virtual Machine.

        Args:
            id (int): Unique identifier
            proc_density (float): Processing density (cycles/bit)
            cpu_freq (float): CPU frequency (cycles/s)
            trans_rate (float): Transmission rate (bits/s)
        """
        self.id = id
        self.proc_density = proc_density
        self.cpu_freq = cpu_freq
        self.trans_rate = trans_rate
        self.assigned_tasks = []
        self.capacity = 5

class Cloud:
    def __init__(self, num_vms):
        """
        Initialize the Cloud with VMs.

        Args:
            num_vms (int): Number of VMs
        """
        self.vms = []
        for i in range(num_vms):
            proc_density = random.uniform(*PROCESSING_DENSITY_VM)
            cpu_freq = random.uniform(*CPU_FREQ_VM) * 1e9
            trans_rate = random.uniform(*TRANSMISSION_RATE) * 1e6
            self.vms.append(VM(i, proc_density, cpu_freq, trans_rate))
        logger.info(f"Initialized Cloud with {num_vms} VMs")

class Network:
    def __init__(self, num_fog_nodes, num_vms):
        """
        Initialize the fog computing network.

        Args:
            num_fog_nodes (int): Number of fog nodes
            num_vms (int): Number of VMs
        """
        self.fog_nodes = []
        for i in range(num_fog_nodes):
            x = (i % 5) * 40 + 20
            y = (i // 5) * 40 + 20
            position = (x, y)
            proc_density = random.uniform(*PROCESSING_DENSITY_FN)
            cpu_freq = random.uniform(*CPU_FREQ_FN) * 1e9
            trans_rate = random.uniform(*TRANSMISSION_RATE) * 1e6
            self.fog_nodes.append(FogNode(i, position, proc_density, cpu_freq, trans_rate))
        self.cloud = Cloud(num_vms)
        # Assign neighbors to each fog node
        for node in self.fog_nodes:
            node.neighbors = self.get_neighbors(node)
        logger.info(f"Initialized Network with {num_fog_nodes} fog nodes and {num_vms} VMs")
        logger.info("Node Positions: %s", [(node.id, node.position) for node in self.fog_nodes])
        logger.info("Neighbors: %s", [(node.id, [n.id for n in node.neighbors]) for node in self.fog_nodes])

    def distance(self, pos1, pos2):
        """
        Calculate Euclidean distance between two positions.

        Args:
            pos1 (tuple): (x, y) coordinates
            pos2 (tuple): (x, y) coordinates

        Returns:
            float: Distance in meters
        """
        return math.sqrt((pos2[0] - pos1[0])**2 + (pos2[1] - pos1[1])**2)

    def get_neighbors(self, node):
        """
        Get neighboring fog nodes within coverage radius.

        Args:
            node (FogNode): The fog node

        Returns:
            list: List of neighboring FogNode objects
        """
        neighbors = []
        for other in self.fog_nodes:
            if other != node and self.distance(node.position, other.position) <= COVERAGE_RADIUS:
                neighbors.append(other)
        return neighbors

    def generate_tasks(self, active_ratio, current_time):
        """
        Generate tasks for active fog nodes.

        Args:
            active_ratio (float): Fraction of nodes to be active
            current_time (float): Current simulation time
        """
        from task import Task  # Local import to avoid circular dependency
        logger.info(f"Generating tasks with active_ratio={active_ratio}, num_nodes={len(self.fog_nodes)}")
        num_active = max(1, int(len(self.fog_nodes) * active_ratio))
        logger.info(f"Selecting {num_active} active nodes")
        active_nodes = random.sample(self.fog_nodes, num_active)
        logger.info(f"Active nodes: {[node.id for node in active_nodes]}")
        task_id = 0
        for node in active_nodes:
            task = Task.generate_task(task_id, node, current_time)
            node.task_queue.append(task)
            logger.info(f"Generated task {task_id} for node {node.id}")
            node.update_role()
            task_id += 1
        logger.info(f"Generated {task_id} tasks for {num_active} active nodes")

    def reset_tasks(self):
        """
        Clear task queues and assigned tasks for all nodes.
        """
        for node in self.fog_nodes:
            node.task_queue = []
            node.assigned_tasks = []
            node.update_role()
        for vm in self.cloud.vms:
            vm.assigned_tasks = []
        logger.info("Reset tasks for all nodes and VMs")