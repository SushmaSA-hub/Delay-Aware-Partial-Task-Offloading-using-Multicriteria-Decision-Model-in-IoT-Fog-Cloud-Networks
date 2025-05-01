from task import Task
from network import FogNode, VM

def compute_min_delay(task, hn_subset):
    """
    Compute the minimal delay for a task when processed by a subset of Helper Nodes or locally.

    Args:
        task (Task): The task to evaluate
        hn_subset (list): Subset of Helper Nodes (FogNode or VM), empty for local processing

    Returns:
        float: The minimal delay in seconds
    """
    delays = []

    # Local processing delay
    local_node = task.tn
    local_proc_time = (task.size * local_node.proc_density) / local_node.cpu_freq
    delays.append(local_proc_time)
    print(f"Local delay for Task {task.id}: proc_time={local_proc_time:.4f}s")

    # Offloaded processing delays
    for hn in hn_subset:
        # Transmission delay
        if isinstance(hn, FogNode):
            trans_time = task.size / min(task.tn.trans_rate, hn.trans_rate)
        else:  # VM
            trans_time = task.size / hn.trans_rate + 0.05  # 50ms cloud latency

        # Processing delay
        proc_time = (task.size * hn.proc_density) / hn.cpu_freq

        # Total delay
        total_delay = trans_time + proc_time
        delays.append(total_delay)
        print(f"Target {hn.id} ({'FogNode' if isinstance(hn, FogNode) else 'VM'}): trans_time={trans_time:.4f}s, proc_time={proc_time:.4f}s, total={total_delay:.4f}s")

    min_delay = min(delays) if delays else float('inf')
    print(f"Min delay for Task {task.id}: {min_delay:.4f}s")
    return min_delay

def compute_processing_efficiency(hn, task):
    """
    Compute the processing efficiency of a Helper Node or VM for a given task.

    Args:
        hn (FogNode or VM): The Helper Node or Virtual Machine
        task (Task): The task to evaluate

    Returns:
        float: Efficiency metric (lower is better)
    """
    # Transmission delay
    if isinstance(hn, FogNode):
        trans_time = task.size / min(task.tn.trans_rate, hn.trans_rate)
    else:  # VM
        trans_time = task.size / hn.trans_rate + 0.05  # 50ms cloud latency

    # Processing delay
    proc_time = (task.size * hn.proc_density) / hn.cpu_freq

    # Total delay
    total_delay = trans_time + proc_time

    # Load factor
    load_factor = 1.0 + (len(hn.assigned_tasks) / hn.capacity)

    return total_delay * load_factor