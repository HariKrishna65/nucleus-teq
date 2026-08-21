import logging
import random
from datetime import datetime, timedelta
from airflow import DAG
from airflow.operators.python import PythonOperator, BranchPythonOperator
from airflow.operators.bash import BashOperator

# Fetch Airflow's standard task logger
logger = logging.getLogger("airflow.task")

# Threat configuration constants
CRITICAL_BREACH_THRESHOLD = 70

default_args = {
    'owner': 'bunker_survivor',
    'depends_on_past': False,
    'start_date': datetime(2026, 1, 1),
    'email_on_failure': False,
    'email_on_retry': False,
    'retries': 1,
    'retry_delay': timedelta(minutes=1),
}

# 03:15 AM every day - Executed during dead of night peak zombie movement
with DAG(
    'zombie_survival_protocol',
    default_args=default_args,
    description='Automated Mid-Night Zombie Defense & Combat Protocol',
    schedule_interval='15 3 * * *', 
    catchup=False,
    tags=['apocalypse', 'zombie_combat'],
) as dag:

    # TASK 1: Scan area for zombie count (Python) -> Pushes horde size to XCom
    def track_zombie_horde(ti):
        logger.info("Reading seismic vibrations and thermal perimeter feeds...")
        zombie_count = random.randint(5, 150)
        
        if zombie_count > 100:
            logger.critical(f"CRITICAL: Massive zombie swarm detected! Count: {zombie_count} heads.")
        elif zombie_count > 40:
            logger.warning(f"ALERT: Moderate zombie wandering patterns observed. Count: {zombie_count}")
        else:
            logger.info(f"INFO: Low zombie presence around immediate area. Count: {zombie_count}")
            
        # Push headcount to XCom for final kill-assessment calculation
        ti.xcom_push(key='tracked_zombie_count', value=zombie_count)

    task_scan_horde = PythonOperator(
        task_id='scan_horde_proximity',
        python_callable=track_zombie_horde,
    )

    # TASK 2: Fire sound decoys to redirect zombies (Bash)
    task_fire_decoys = BashOperator(
        task_id='check_decoy_flares',
        bash_command='echo "INFO: Launching sonic distraction canisters..." && sleep 1 && echo "SUCCESS: Audio decoys activated 500m North of base."',
    )

    # TASK 3: Check structural damage from zombies banging on walls (Python) -> Pushes risk score to XCom
    def check_barricades(ti):
        logger.info("Analyzing structural pressure sensors on North and South gates...")
        breach_risk = random.randint(10, 100) # Percentage risk
        
        if breach_risk >= CRITICAL_BREACH_THRESHOLD:
            logger.critical(f"CRITICAL: Outer fences buckling under zombie pressure! Breach Risk: {breach_risk}%")
        else:
            logger.info(f"INFO: Structural reinforcement holding steady. Breach Risk: {breach_risk}%")
            
        # Push risk to XCom for branching decisions
        ti.xcom_push(key='wall_breach_risk_score', value=breach_risk)

    task_measure_walls = PythonOperator(
        task_id='measure_wall_integrity',
        python_callable=check_barricades,
    )

    # TASK 4: Emergency internal lockdown via Bash commands (Bash)
    task_seal_inner = BashOperator(
        task_id='seal_inner_bunkers',
        bash_command='echo "WARNING: Pre-locking interior panic room bulkheads..." && sleep 2 && echo "SUCCESS: Security seals online."',
    )

    # TASK 5: Branching mechanism based on structural wall danger from Task 3 (BranchPython)
    def branch_combat_posture(ti):
        risk = ti.xcom_pull(task_ids='measure_wall_integrity', key='wall_breach_risk_score')
        logger.info(f"Retrieving wall stress metrics from XCom. Breach risk is: {risk}%")
        
        if risk >= CRITICAL_BREACH_THRESHOLD:
            logger.warning(f"Risk {risk}% >= Threshold {CRITICAL_BREACH_THRESHOLD}%. INITIATING COMBAT DEFENSES!")
            return 'activate_claymore_mines'
        else:
            logger.info(f"Risk {risk}% is within safe limits. Bypassing explosives to save limited ammunition.")
            return 'hold_ammunition_reserves'

    task_analyze_combat = BranchPythonOperator(
        task_id='analyze_combat_necessity',
        python_callable=branch_combat_posture,
    )

    # TASK 6: Explosive defense path (Bash) -> Executed ONLY if breach risk is high
    task_detonate_mines = BashOperator(
        task_id='activate_claymore_mines',
        bash_command='echo "CRITICAL: Detonating perimeter Claymore mine grid!" && sleep 2 && echo "SUCCESS: Shockwave cleared fence lines."',
    )

    # TASK 7: Ammo conservation path (Bash) -> Executed ONLY if breach risk is low
    task_hold_ammo = BashOperator(
        task_id='hold_ammunition_reserves',
        bash_command='echo "INFO: Weapons remaining cold. Physical gates holding fine without ammo expenditure."',
    )

    # TASK 8: Assess estimated zombie fatalities using initial data from Task 1 (Python)
    def calculate_casualties(ti):
        # Pull original zombie count from Task 1
        starting_zombies = ti.xcom_pull(task_ids='scan_horde_proximity', key='tracked_zombie_count')
        logger.info(f"Pulling initial swarm data from XCom... {starting_zombies} zombies were outside.")
        
        # Check if we fired mines or saved ammo to calculate remaining threats
        # Airflow ti.xcom_pull can check status or we can base it on dynamic narrative logic
        if starting_zombies > 50:
            kill_count = int(starting_zombies * 0.85) # Cleared 85% with defenses
            logger.warning(f"COMBAT ASSESSMENT: Defense grid successfully eliminated roughly {kill_count} zombies tonight.")
        else:
            logger.info("COMBAT ASSESSMENT: No major defensive expenditure needed. Numbers remain stable.")

    task_assess_casualties = PythonOperator(
        task_id='assess_horde_casualities',
        python_callable=calculate_casualties,
        trigger_rule='one_success', # Runs after either task 6 or 7 completes
    )

    # Zombie Defense Pipeline Topology
    task_scan_horde >> task_fire_decoys >> task_measure_walls >> task_seal_inner >> task_analyze_combat >> [task_detonate_mines, task_hold_ammo] >> task_assess_casualties
