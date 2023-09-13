# Techno Trojan Training - Drivetrain (Mecanum)

Welcome!

The Techno Trojan Training series provides a series of program repositories that provide
working examples for training new developers. These public modules will be updated and maintained
as the FIRST FTC framework is updated.

## Downloading the Project

### TechnoTrojanTraining (TTT) - Training Series Repository

This repository is intended to be used as a training guide to help when building out your own competition
program. This repository is not intended to be used 'as-is' for any competition robot codebase. However, the
team code within this repository can be used to bring into your own project and adapted to your own robot configuration.

The base project is from the FtcRobotController project/repository in the FIRST-Tech-Challenge Github organization.
The modules that have been added (TeamCode) as part of this training project are detailed individually below. You can either download the entire (TechnoTrojanTraining) project
or simply grab the specific java program modules you are interested in.

### FtcRobotController Framework

> Reference: [FtcRobotController GitHub Repository](https://github.com/FIRST-Tech-Challenge/FtcRobotController)
> Release Version: 9.0
> Season: 2023/2024 (competition season)
> Game: Centerstage

### Obtaining This Repository (TechnoTrojanTraining) Code

If you are familiar with GitHub (and have an account) you can clone this repository locally. Since this project is derived from the
FIRST FtcRobotController project, any project base you are working on for your own competition robot should be taken from that
repository. I would recommend cloning the FtcRobotController project first, then add the TeamCode contents from this project. This
would give you a base project that includes the latest FtcRobotController framework with a working field-centric mecanum drivetrain.

> ***git clone Reference:*** https://github.com/FTCTeam11531/TechnoTrojanTraining_Drivetrain_Mecanum.git

Or, if you prefer, you can download the entire project code. Click on the 'Download Zip' link under 'Code'.

If you only need the TeamCode modules you can navigate to the java program module of interest, click the elipse '...', then click download (or ctrl + shift + s). Each
TeamCode module will be explained below.

There are no releases built since this is a training module; however, in the future we may create releases when the FtcRobotController framework version changes.

### Java Module(s)

Each TeamCode java program module added to the base FtcRobotController project/framework is defined below.

When navigating to a specific java program module reference the path 'TeamCode/src/main/java' then the listed package path.

#### Subsystem - Drivetrain

> ***Package:*** org.firstinspires.ftc.teamcode.system  
> ***Class name:*** sysDrivetrainMecanum.java
>
> Documentation coming soon
>

#### Subsystem - Lighting

> ***Package:*** org.firstinspires.ftc.teamcode.system  
> ***Class name:*** sysLighting.java
>
> Documentation coming soon
>

#### Utility - Robot Constants

> ***Package:*** org.firstinspires.ftc.teamcode.utility  
> ***Class name:*** utilRobotConstants.java
>
> Documentation coming soon
>

#### Enumeration - Robot Speed

> ***Package:*** org.firstinspires.ftc.teamcode.utility  
> ***Class name:*** enumStateDriveMotorMaxOutputPower.java
>
> Documentation coming soon
>

#### Enumeration - Robot Drive Mode

> ***Package:*** org.firstinspires.ftc.teamcode.utility  
> ***Class name:*** enumStateDrivetrainMode.java
>
> Documentation coming soon
>

#### OpMode - Teleop - Main Routine

> ***Package:*** org.firstinspires.ftc.teamcode.opmode  
> ***Class name:*** opmodeTeleopMain.java
>
> Documentation coming soon
>

## Running OpMode: Teleop - 'TTT: Drivetrain Mecanum'

After deploying the project to the Rev control hub this opMode can be found under the TeleOp mode selection
with the title 'TTT: Drivetrain Mecanum'.

### Controls

#### Gamepad1 - Main Driver

Robot Movement
- Axis (left_stick_x, left_stick_y): Drive
- Axis (right_stick_x): Rotate
- Y: Set Output Speed to Med
- A: Set Output Speed to Low

Robot Movement - Function(s) ***not in place!***
- X: 180 spin

Override Settings
- D-Pad Up + X: Reset Heading Override (and Raw)
- Back: Switch between Field-Centric and Robot-Centric drive ***not in place!***

#### Gamepad2 - Co-Driver

NA - This program does not have any functions that require a co-driver/second gamepad

### OpMode Details

#### Inertial Measurement Unit (IMU) - Location Planning

If the hub containing the IMU you are using is mounted so that the "REV" logo does
not face up, remap the IMU axes so that the z-axis points upward (normal to the floor.)

               | +Z axis
               |
               |
               |
        _______|_____________     +Y axis (roll)
       /       |____________//__________
      /   REV / CONTROL    //
     /       / HUB        //
    /_______/____________//
    |______/____________|/
          /
         / +X axis (pitch)

This diagram is derived from the axes in section 3.4 [Bosch Sensor Datasheet](https://www.bosch-sensortec.com/media/boschsensortec/downloads/datasheets/bst-bno055-ds000.pdf)
and the placement of the dot/orientation from [Rev Robotics - Control Hub Overview](https://docs.revrobotics.com/duo-control/control-system-overview/dimensions#imu-location)

## Additional Information

Documentation coming soon

