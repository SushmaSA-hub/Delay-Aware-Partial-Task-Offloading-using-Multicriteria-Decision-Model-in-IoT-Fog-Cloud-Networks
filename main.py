import simpy
from network import Network, FogNode
from offloading import Offloader
from config import ACTIVE_RATIOS, NUM_ROUNDS
import statistics
import logging
import sys
from datetime import datetime
import matplotlib.pyplot as plt

# Configure logging
log_file = f"simulation_{datetime.now().strftime('%Y%m%d_%H%M%S')}.log"
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler(log_file),
        logging.StreamHandler(sys.stdout)
    ]
)
logger = logging.getLogger()

def simulation(env, network, offloader, active_ratio, round_num):
    """
    Run one round of simulation for task offloading.

    Args:
        env (simpy.Environment): SimPy environment
        network (Network): Fog computing network
        offloader (Offloader): Offloading manager
        active_ratio (float): Ratio of active nodes
        round_num (int): Round number for logging
    """
    try:
        logger.info(f"Round {round_num}: Starting task generation...")
        network.generate_tasks(active_ratio, env.now)
        task_queues = [(node.id, len(node.task_queue), node.role) for node in network.fog_nodes]
        logger.info(f"Round {round_num}: Task Queues: {task_queues}")

        if not any(node.task_queue for node in network.fog_nodes):
            logger.warning(f"Round {round_num}: No tasks generated")
            return []

        logger.info(f"Round {round_num}: Running matching...")
        assignments = offloader.m2o_matching()
        assignments_log = [(task.id, [f"FogNode {t.id}" if isinstance(t, FogNode) else f"VM {t.id}" for t in targets]) for task, targets in assignments.items()]
        logger.info(f"Round {round_num}: Assignments: {assignments_log}")

        if not assignments:
            logger.warning(f"Round {round_num}: No assignments made")
            return []

        logger.info(f"Round {round_num}: Running OTOS...")
        results = yield env.process(offloader.otos(assignments))

        delays = []
        logger.info(f"Round {round_num}: Offloading Results:")
        for task, subtasks in results.items():
            logger.info(f"Task {task.id} at {task.tn.id}:")
            for target, size, delay in subtasks:
                target_name = f"FogNode {target.id}" if isinstance(target, FogNode) else f"VM {target.id}" if isinstance(target, VM) else f"TN {target.id}"
                logger.info(f"  {size/8e6:.2f} MB to {target_name}, Delay: {delay:.4f} s")
            max_delay = max(delay for _, _, delay in subtasks)
            delays.append(max_delay)
            logger.info(f"  Total Delay: {max_delay:.4f} s")

        yield env.timeout(1)
        return delays
    except Exception as e:
        logger.error(f"Round {round_num}: Error in simulation: {e}")
        return []

def main(env, network, offloader):
    """
    Run multiple rounds of simulation and visualize results.

    Args:
        env (simpy.Environment): SimPy environment
        network (Network): Fog computing network
        offloader (Offloader): Offloading manager
    """
    all_delays = []
    round_delays = []
    node_utilization = {node.id: 0 for node in network.fog_nodes}
    total_subtasks = 0

    for round in range(NUM_ROUNDS):
        logger.info(f"\nRound {round + 1}")
        result_event = simpy.Event(env)
        def run_simulation():
            delays = yield env.process(simulation(env, network, offloader, active_ratio=0.5, round_num=round + 1))
            result_event.succeed(delays)
        env.process(run_simulation())
        delays = yield result_event
        all_delays.extend(delays)
        round_delays.append(statistics.mean(delays) if delays else 0)
        logger.info(f"Round {round + 1}: Resetting tasks...")
        for node in network.fog_nodes:
            node_utilization[node.id] += len(node.assigned_tasks)
            total_subtasks += len(node.assigned_tasks)
            node.assigned_tasks = []
        network.reset_tasks()
        yield env.timeout(1)

    if all_delays:
        logger.info(f"\nAverage Delay: {statistics.mean(all_delays):.4f} s")
        logger.info(f"Std Dev Delay: {statistics.stdev(all_delays):.4f} s")

        # Plot delay distribution
        plt.figure(figsize=(10, 6))
        plt.hist(all_delays, bins=20, edgecolor='black')
        plt.title('Delay Distribution Across All Tasks')
        plt.xlabel('Delay (s)')
        plt.ylabel('Frequency')
        plt.grid(True)
        delay_plot_file = f"delay_distribution_{datetime.now().strftime('%Y%m%d_%H%M%S')}.png"
        plt.savefig(delay_plot_file)
        plt.close()
        logger.info(f"Delay distribution plot saved to {delay_plot_file}")

        # Plot average delay per round
        plt.figure(figsize=(10, 6))
        plt.plot(range(1, NUM_ROUNDS + 1), round_delays, marker='o')
        plt.title('Average Delay per Round')
        plt.xlabel('Round')
        plt.ylabel('Average Delay (s)')
        plt.grid(True)
        round_delay_plot_file = f"round_delay_{datetime.now().strftime('%Y%m%d_%H%M%S')}.png"
        plt.savefig(round_delay_plot_file)
        plt.close()
        logger.info(f"Round delay plot saved to {round_delay_plot_file}")

        # Plot node utilization
        utilization = [node_utilization[node_id] / total_subtasks if total_subtasks else 0 for node_id in sorted(node_utilization)]
        plt.figure(figsize=(10, 6))
        plt.bar(range(len(utilization)), utilization)
        plt.title('Fog Node Utilization')
        plt.xlabel('Fog Node ID')
        plt.ylabel('Fraction of Total Subtasks')
        plt.grid(True)
        utilization_plot_file = f"utilization_{datetime.now().strftime('%Y%m%d_%H%M%S')}.png"
        plt.savefig(utilization_plot_file)
        plt.close()
        logger.info(f"Utilization plot saved to {utilization_plot_file}")
    else:
        logger.info("No delays collected")

if __name__ == "__main__":
    env = simpy.Environment()
    network = Network(num_fog_nodes=10, num_vms=5)
    offloader = Offloader(network, env)
    logger.info("Starting main process...")
    logger.info(f"Node Positions: {[(node.id, node.position) for node in network.fog_nodes]}")
    # Log neighbors with error handling
    try:
        logger.info(f"Neighbors: {[(node.id, [n.id for n in node.neighbors]) for node in network.fog_nodes]}")
    except AttributeError as e:
        logger.error(f"Failed to log neighbors: {e}")
    logger.info(f"Event queue: {env._queue}")
    env.process(main(env, network, offloader))
    env.run(until=100)
    logger.info(f"Simulation complete. Output saved to {log_file}")