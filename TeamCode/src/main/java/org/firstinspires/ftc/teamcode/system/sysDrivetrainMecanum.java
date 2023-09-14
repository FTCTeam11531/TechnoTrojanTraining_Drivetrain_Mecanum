package org.firstinspires.ftc.teamcode.system;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.MotorConfigurationType;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.teamcode.utility.utilRobotConstants;
import org.firstinspires.ftc.teamcode.utility.enumStateDrivetrainMode;
import org.firstinspires.ftc.teamcode.utility.enumStateDriveMotorMaxOutputPower;

import java.util.Arrays;
import java.util.List;

public class sysDrivetrainMecanum {

    // System Op Mode
    private LinearOpMode sysOpMode = null;

    // Define Drivetrain Mode Enumerator
    public enumStateDrivetrainMode stateDrivetrainMode;

    // Define Drivetrain Output Power Enumerator
    public enumStateDriveMotorMaxOutputPower stateDriveMotorMaxOutputPower;

    // Define Hardware
    private DcMotorEx leftFrontDrive, rightFrontDrive, leftBackDrive, rightBackDrive;
    private List<DcMotorEx> listMotorsDrivetrain;

    // Robot Heading
    private BNO055IMU imuUnit = null;
    BNO055IMU.Parameters imuParameters;
    private double trackHeadingRobot, trackHeadingOffset, trackHeadingError;

    public sysDrivetrainMecanum(LinearOpMode inOpMode) {
        sysOpMode = inOpMode;
    }

    public void init() {
        // Set Drivetrain Enumerator default value(s)
        stateDrivetrainMode = enumStateDrivetrainMode.Field_Centric;
        stateDriveMotorMaxOutputPower = enumStateDriveMotorMaxOutputPower.Low;

        // Define and Initialize Motors (note: need to use reference to actual OpMode).
        // Initialize the hardware variables. Note that the strings used here must correspond
        // to the names assigned during the robot configuration step on the DS or RC devices.
        leftFrontDrive  = sysOpMode.hardwareMap.get(DcMotorEx.class, utilRobotConstants.Configuration.LABEL_DRIVETRAIN_MOTOR_LEFT_FRONT);
        rightFrontDrive = sysOpMode.hardwareMap.get(DcMotorEx.class, utilRobotConstants.Configuration.LABEL_DRIVETRAIN_MOTOR_RIGHT_FRONT);
        leftBackDrive  = sysOpMode.hardwareMap.get(DcMotorEx.class, utilRobotConstants.Configuration.LABEL_DRIVETRAIN_MOTOR_LEFT_BACK);
        rightBackDrive = sysOpMode.hardwareMap.get(DcMotorEx.class, utilRobotConstants.Configuration.LABEL_DRIVETRAIN_MOTOR_RIGHT_BACK);

        // Add Motors to an Array/List of Motors
        listMotorsDrivetrain = Arrays.asList(leftFrontDrive, rightFrontDrive, leftBackDrive, rightBackDrive);

        // Clone Configuration and apply to all Motors in the list (set max RPM to 100%)
        for (DcMotorEx itemMotor : listMotorsDrivetrain) {
            MotorConfigurationType motorConfigurationType = itemMotor.getMotorType().clone();
            motorConfigurationType.setAchieveableMaxRPMFraction(1.0);
            itemMotor.setMotorType(motorConfigurationType);
        }

        // Set Zero Setting to Brake Mode
        setDriveMotorZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);

        // To drive forward, most robots need the motor on one side to be reversed, because the axles point in opposite directions.
        // Pushing the left stick forward MUST make robot go forward. So adjust these two lines based on your first test drive.
        // Note: The settings here assume direct drive on left and right wheels.  Gear Reduction or 90 Deg drives may require direction flips
        // Reverse the right side motors
        // Reverse left motors if you are using NeveRests
        leftFrontDrive.setDirection(DcMotorEx.Direction.FORWARD);
        rightFrontDrive.setDirection(DcMotorEx.Direction.REVERSE);
        leftBackDrive.setDirection(DcMotorEx.Direction.FORWARD);
        rightBackDrive.setDirection(DcMotorEx.Direction.REVERSE);

        // If the hub containing the IMU you are using is mounted so that the "REV" logo does
        // not face up, remap the IMU axes so that the z-axis points upward (normal to the floor.)
        //
        //             | +Z axis
        //             |
        //             |
        //             |
        //      _______|_____________     +Y axis (roll)
        //     /       |_____________/|__________
        //    /   REV / EXPANSION   //
        //   /       / HUB         //
        //  /_______/_____________//
        // |_______/_____________|/
        //        /
        //       / +X axis (pitch)
        //
        // This diagram is derived from the axes in section 3.4 https://www.bosch-sensortec.com/media/boschsensortec/downloads/datasheets/bst-bno055-ds000.pdf
        // and the placement of the dot/orientation from https://docs.revrobotics.com/rev-control-system/control-system-overview/dimensions#imu-location

        // Initialize the IMU board/unit on the Rev Control Hub
        imuUnit = sysOpMode.hardwareMap.get(BNO055IMU.class, utilRobotConstants.Configuration.LABEL_CONTROLHUB_IMU);
        imuParameters = new BNO055IMU.Parameters();

        // Set the Angle Unit to Radians
        imuParameters.angleUnit = BNO055IMU.AngleUnit.RADIANS;

        // Initialize the IMU unit
        imuUnit.initialize(imuParameters);

        // Reset Drive Motor Encoder(s)
        setDriveMotorRunMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        resetRobotHeading();

        // Display telemetry
        sysOpMode.telemetry.addData(">", "------------------------------------");
        sysOpMode.telemetry.addData(">", " System: Drivetrain Initialized");
        sysOpMode.telemetry.update();
    }


    /**
     * <h2>Drivetrain Method: driveMecanum</h2>
     * <hr>
     * <b>Author:</b> {@value utilRobotConstants.About#COMMENT_AUTHOR_NAME}<br>
     * <b>Season:</b> {@value utilRobotConstants.About#COMMENT_SEASON_PERIOD}<br>
     * <hr>
     * <p>
     * Holonomic drives provide the ability for the robot to move in three axes (directions) simultaneously.
     * Each motion axis is controlled by one Joystick axis.
     * </p>
     * <p>
     * This is a standard mecanum drivetrain or a 'Robot Centric' drivetrain
     * </p>
     * <br>
     * <i>[Y]</i> <b>Axial:</b>   Driving forward and backward<br>
     * <i>[X]</i> <b>Lateral:</b> Strafing right and left<br>
     * <i>[R]</i> <b>Yaw:</b>   Rotating Clockwise and counter clockwise<br>
     *
     * @param inAxial   [Y] Driving forward and backward
     * @param inLateral [X] Strafing right and left
     * @param inYaw     [R] Rotating Clockwise and counter clockwise
     * @param inMaxOutputPowerPercent Percent of power to apply to motors
     *
     * <br>
     */
    public void driveMecanum(double inAxial, double inLateral, double inYaw, double inMaxOutputPowerPercent) {

        double modMaintainMotorRatio;

        double inputAxial   = (inAxial * inMaxOutputPowerPercent);  // Note: pushing stick forward gives negative value
        double inputLateral = (inLateral * inMaxOutputPowerPercent) * utilRobotConstants.Drivetrain.MOTOR_LATERAL_MOVEMENT_STRAFING_CORRECTION; // Mod to even out strafing
        double inputYaw     = (inYaw * inMaxOutputPowerPercent);

        // Normalize the values so no wheel power exceeds 100%
        // This ensures that the robot maintains the desired motion.
        modMaintainMotorRatio = Math.max(Math.abs(inputAxial) + Math.abs(inputLateral) + Math.abs(inputYaw), utilRobotConstants.Drivetrain.MOTOR_OUTPUT_POWER_MAX);

        // Combine the joystick requests for each axis-motion to determine each wheel's power.
        // Set up a variable for each drive wheel to save the power level for telemetry.
        double leftFrontPower  = (inputAxial + inputLateral + inputYaw) / modMaintainMotorRatio;
        double rightFrontPower = (inputAxial - inputLateral - inputYaw) / modMaintainMotorRatio;
        double leftBackPower   = (inputAxial - inputLateral + inputYaw) / modMaintainMotorRatio;
        double rightBackPower  = (inputAxial + inputLateral - inputYaw) / modMaintainMotorRatio;

        // Use existing function to drive both wheels.
        setDriveMotorPower(leftFrontPower, rightFrontPower, leftBackPower, rightBackPower);
    }

    /**
     * <h2>Drivetrain Method: driveMecanumFieldCentric</h2>
     * <hr>
     * <b>Author:</b> {@value utilRobotConstants.About#COMMENT_AUTHOR_NAME}<br>
     * <b>Season:</b> {@value utilRobotConstants.About#COMMENT_SEASON_PERIOD}<br>
     * <hr>
     * <p>
     * Holonomic drives provide the ability for the robot to move in three axes (directions) simultaneously.
     * Each motion axis is controlled by one Joystick axis.
     * </p>
     * <br>
     * <p>
     * This is a 'Field Centric' variation of the mecanum drivetrain
     * </p>
     * <br>
     * <i>[Y]</i> <b>Axial:</b>   Driving forward and backward<br>
     * <i>[X]</i> <b>Lateral:</b> Strafing right and left<br>
     * <i>[R]</i> <b>Yaw:</b>   Rotating Clockwise and counter clockwise<br>
     *
     * @param inAxial   [Y] Driving forward and backward
     * @param inLateral [X] Strafing right and left
     * @param inYaw     [R] Rotating Clockwise and counter clockwise
     * @param inMaxOutputPowerPercent Percent of power to apply to motors
     *
     * <br>
     */
    public void driveMecanumFieldCentric(double inAxial, double inLateral, double inYaw, double inMaxOutputPowerPercent) {

        double modMaintainMotorRatio;

        double inputAxial   = (inAxial * inMaxOutputPowerPercent);  // Note: pushing stick forward gives negative value
        double inputLateral = (inLateral * inMaxOutputPowerPercent) * utilRobotConstants.Drivetrain.MOTOR_LATERAL_MOVEMENT_STRAFING_CORRECTION; // Mod to even out strafing
        double inputYaw     = (inYaw * inMaxOutputPowerPercent);

        // Get heading value from the IMU
        double botHeading = getRobotHeadingAdj();

        // Adjust the lateral and axial movements based on heading
        double adjLateral = inputLateral * Math.cos(botHeading) - inputAxial * Math.sin(botHeading);
        double adjAxial = inputLateral * Math.sin(botHeading) + inputAxial * Math.cos(botHeading);

        // Normalize the values so no wheel power exceeds 100%
        // This ensures that the robot maintains the desired motion.
        modMaintainMotorRatio = Math.max(Math.abs(inputAxial) + Math.abs(inputLateral) + Math.abs(inputYaw), utilRobotConstants.Drivetrain.MOTOR_OUTPUT_POWER_MAX);

        // Combine the joystick requests for each axis-motion to determine each wheel's power.
        // Set up a variable for each drive wheel to save the power level for telemetry.
        double leftFrontPower  = (adjAxial + adjLateral + inputYaw) / modMaintainMotorRatio;
        double rightFrontPower = (adjAxial - adjLateral - inputYaw) / modMaintainMotorRatio;
        double leftBackPower   = (adjAxial - adjLateral + inputYaw) / modMaintainMotorRatio;
        double rightBackPower  = (adjAxial + adjLateral - inputYaw) / modMaintainMotorRatio;

        // Use existing function to drive both wheels.
        setDriveMotorPower(leftFrontPower, rightFrontPower, leftBackPower, rightBackPower);
    }




    /**
     * <h2>Drivetrain Method: resetRobotHeading</h2>
     * <hr>
     * <b>Author:</b> {@value utilRobotConstants.About#COMMENT_AUTHOR_NAME}<br>
     * <b>Season:</b> {@value utilRobotConstants.About#COMMENT_SEASON_PERIOD}<br>
     * <hr>
     * <p>
     * Reset the output heading value from the IMU.
     * </p>
     */
    public void resetRobotHeading() {

        // Set the Heading Offset to the IMU raw heading
        trackHeadingOffset = getRobotHeadingRaw();

        // Reset the Robot Heading to Zero
        trackHeadingRobot = 0;
    }

    public void resetZeroRobotHeading() {

        // Set the Heading Offset to the IMU raw heading
        imuUnit.initialize(imuParameters);
        utilRobotConstants.CommonSettings.setImuTransitionAdjustment(0);
    }

    /**
     * <h2>Drivetrain Method: getRobotHeadingRaw</h2>
     * <hr>
     * <b>Author:</b> {@value utilRobotConstants.About#COMMENT_AUTHOR_NAME}<br>
     * <b>Season:</b> {@value utilRobotConstants.About#COMMENT_SEASON_PERIOD}<br>
     * <hr>
     * <p>
     * Get the output heading value from the IMU.
     * </p>
     * @return double - Output heading value from the IMU - raw reading
     */
    public double getRobotHeadingRaw() {
        // Variable for output heading value
        double outRobotHeadingValue;

        // Get heading value from the IMU
        // Read inverse IMU heading, as the IMU heading is CW positive
        outRobotHeadingValue = -(imuUnit.getAngularOrientation().firstAngle);
//        outRobotHeadingValue = imuUnit.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES).firstAngle;

        // Should the IMU heading be inversed? Does it matter?
        // Will need to view the heading readout on the driver hub

        //imuUnit.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES).firstAngle

        return outRobotHeadingValue;
    }

    /**
     * <h2>Drivetrain Method: getRobotHeadingAdj</h2>
     * <hr>
     * <b>Author:</b> {@value utilRobotConstants.About#COMMENT_AUTHOR_NAME}<br>
     * <b>Season:</b> {@value utilRobotConstants.About#COMMENT_SEASON_PERIOD}<br>
     * <hr>
     * <p>
     * Get the output heading value from the IMU (with offset adjustment).
     * </p>
     * @return double - Output heading value from the IMU (with offset adjustment)
     * <br>
     */
    public double getRobotHeadingAdj() {
        // Variable for output heading value
        double outRobotHeadingValue;

        // Get heading value from the IMU
        // Read inverse IMU heading, as the IMU heading is CW positive
        outRobotHeadingValue = getRobotHeadingRaw() + utilRobotConstants.CommonSettings.getImuTransitionAdjustment();

        // Should the IMU heading be inversed? Does it matter?
        // Will need to view the heading readout on the driver hub

        //imuUnit.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES).firstAngle

        return outRobotHeadingValue;
    }

    /**
     * <h2>Drivetrain Method: getSteeringCorrection</h2>
     * <hr>
     * <b>Author:</b> {@value utilRobotConstants.About#COMMENT_AUTHOR_NAME}<br>
     * <b>Season:</b> {@value utilRobotConstants.About#COMMENT_SEASON_PERIOD}<br>
     * <hr>
     * <p>
     * Get steering correction value based on the target heading and P gain from control loop.
     * </p>
     * @param inTargetHeader
     * @param inProportionalGain
     * @return
     */
    public double getSteeringCorrection(double inTargetHeader, double inProportionalGain) {

        // Get robot header by subtracking the offset from the heading
        trackHeadingRobot = imuUnit.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES).firstAngle;

        // Determine the heading current error
        trackHeadingError = inTargetHeader - trackHeadingRobot;

        // Normalize the error to be within +/- 180 degrees
//        while (trackHeadingError > 180) trackHeadingError -= 360;
//        while (trackHeadingError <= -180) trackHeadingError += 360;

        return Range.clip(trackHeadingError * inProportionalGain, -1, 1);
    }

    /**
     * <h2>Drivetrain Method: getDrivetrainMotorPower</h2>
     * <hr>
     * <b>Author:</b> {@value utilRobotConstants.About#COMMENT_AUTHOR_NAME}<br>
     * <b>Season:</b> {@value utilRobotConstants.About#COMMENT_SEASON_PERIOD}<br>
     * <hr>
     * <p>
     * Get the output power value for a drivetrain motor.
     * </p>
     * @param inMotorLabel  The Label Name of the motor to get the power value from
     * @return double - Output power value of the motor
     * <br>
     */
    public double getDrivetrainMotorPower(String inMotorLabel) {
        // Variable for output Power value for drivetrain motor(s)
        double outPowerValue;

        // Get value for motor specified in method call
        switch (inMotorLabel) {
            // Drivetrain Motor - Left Front
            case utilRobotConstants.Configuration.LABEL_DRIVETRAIN_MOTOR_LEFT_FRONT:
                outPowerValue = leftFrontDrive.getPower();
                break;
            // Drivetrain Motor - Left Back
            case utilRobotConstants.Configuration.LABEL_DRIVETRAIN_MOTOR_LEFT_BACK:
                outPowerValue = leftBackDrive.getPower();
                break;
            // Drivetrain Motor - Right Front
            case utilRobotConstants.Configuration.LABEL_DRIVETRAIN_MOTOR_RIGHT_FRONT:
                outPowerValue = rightFrontDrive.getPower();
                break;
            // Drivetrain Motor - Right Back
            case utilRobotConstants.Configuration.LABEL_DRIVETRAIN_MOTOR_RIGHT_BACK:
                outPowerValue = rightBackDrive.getPower();
                break;
            // Default - No match
            default:
                outPowerValue = 0;
        }

        // Return value
        return outPowerValue;
    }

    /**
     * <h2>Drivetrain Method: getDrivetrainMotorEncoderPosition</h2>
     * <hr>
     * <b>Author:</b> {@value utilRobotConstants.About#COMMENT_AUTHOR_NAME}<br>
     * <b>Season:</b> {@value utilRobotConstants.About#COMMENT_SEASON_PERIOD}<br>
     * <hr>
     * <p>
     * Get the value of the current encoder position for a drivetrain motor.
     * </p>
     * @param inMotorLabel - String - hardware label for the motor to return value from
     * @return int - Encoder position for the specified motor
     */
    public int getDrivetrainMotorEncoderPosition(String inMotorLabel) {
        // Variable for output Encoder value for drivetrain motor(s)
        int outEncoderValue;

        // Get value for motor specified in method call
        switch (inMotorLabel) {
            // Drivetrain Motor - Left Front
            case utilRobotConstants.Configuration.LABEL_DRIVETRAIN_MOTOR_LEFT_FRONT:
                outEncoderValue = leftFrontDrive.getCurrentPosition();
                break;
            // Drivetrain Motor - Left Back
            case utilRobotConstants.Configuration.LABEL_DRIVETRAIN_MOTOR_LEFT_BACK:
                outEncoderValue = leftBackDrive.getCurrentPosition();
                break;
            // Drivetrain Motor - Right Front
            case utilRobotConstants.Configuration.LABEL_DRIVETRAIN_MOTOR_RIGHT_FRONT:
                outEncoderValue = rightFrontDrive.getCurrentPosition();
                break;
            // Drivetrain Motor - Right Back
            case utilRobotConstants.Configuration.LABEL_DRIVETRAIN_MOTOR_RIGHT_BACK:
                outEncoderValue = rightBackDrive.getCurrentPosition();
                break;
            // Default - No match
            default:
                outEncoderValue = 0;
        }

        // Return value
        return outEncoderValue;
    }

    /**
     * <h2>Drivetrain Method: getLabelDrivetrainMode</h2>
     * <hr>
     * <b>Author:</b> {@value utilRobotConstants.About#COMMENT_AUTHOR_NAME}<br>
     * <b>Season:</b> {@value utilRobotConstants.About#COMMENT_SEASON_PERIOD}<br>
     * <hr>
     * <p>
     * Get the value of the current Drivetrain mode from List Iterator.
     * </p>
     * @return String - Output the current mode value
     * <br>
     */
    public String getLabelDrivetrainMode() {
        return stateDrivetrainMode.getLabel();
    }

    /**
     * <h2>Drivetrain Method: getLabelDrivetrainOutputPower</h2>
     * <hr>
     * <b>Author:</b> {@value utilRobotConstants.About#COMMENT_AUTHOR_NAME}<br>
     * <b>Season:</b> {@value utilRobotConstants.About#COMMENT_SEASON_PERIOD}<br>
     * <hr>
     * <p>
     * Get the label of the current Drivetrain output power setting from List Iterator.
     * </p>
     * @return String - Output the current output power setting label
     * <br>
     */
    public String getLabelDrivetrainOutputPower() {
        return stateDriveMotorMaxOutputPower.getLabel();
    }

    /**
     * <h2>Drivetrain Method: getDrivetrainOutputPowerCurrent</h2>
     * <hr>
     * <b>Author:</b> {@value utilRobotConstants.About#COMMENT_AUTHOR_NAME}<br>
     * <b>Season:</b> {@value utilRobotConstants.About#COMMENT_SEASON_PERIOD}<br>
     * <hr>
     * <p>
     * Get the value of the current Drivetrain output power setting from List Iterator.
     * </p>
     * @return double - Output the current output power setting value
     * <br>
     */
    public double getValueDrivetrainOutputPower() { return stateDriveMotorMaxOutputPower.getValue(); }

    /**
     * <h2>Drivetrain Method: setDrivetrainModeNext</h2>
     * <hr>
     * <b>Author:</b> {@value utilRobotConstants.About#COMMENT_AUTHOR_NAME}<br>
     * <b>Season:</b> {@value utilRobotConstants.About#COMMENT_SEASON_PERIOD}<br>
     * <hr>
     * <p>
     * Get the next value of the Drivetrain mode from List Iterator.
     * </p>
     * <br>
     */
    public void setDrivetrainModeNext() {

        // Cycle drivetrain mode
        stateDrivetrainMode =  stateDrivetrainMode.nextState();
        sysOpMode.sleep(utilRobotConstants.CommonSettings.SLEEP_TIMER_MILLISECONDS_DEFAULT);
    }

    /**
     * <h2>Drivetrain Method: setDrivetrainOutputPowerNext</h2>
     * <hr>
     * <b>Author:</b> {@value utilRobotConstants.About#COMMENT_AUTHOR_NAME}<br>
     * <b>Season:</b> {@value utilRobotConstants.About#COMMENT_SEASON_PERIOD}<br>
     * <hr>
     * <p>
     * Set the next value of the Drivetrain output power setting from List Iterator.
     * </p>
     * <br>
     */
    public void setDrivetrainOutputPowerNext() {

        // Cycle drivetrain output power
        stateDriveMotorMaxOutputPower = stateDriveMotorMaxOutputPower.nextState();
        sysOpMode.sleep(utilRobotConstants.CommonSettings.SLEEP_TIMER_MILLISECONDS_DEFAULT);
    }

    /**
     * <h2>Drivetrain Method: setDriveMotorPower</h2>
     * <hr>
     * <b>Author:</b> {@value utilRobotConstants.About#COMMENT_AUTHOR_NAME}<br>
     * <b>Season:</b> {@value utilRobotConstants.About#COMMENT_SEASON_PERIOD}<br>
     * <hr>
     * <p>
     * Pass the requested wheel motor powers to the appropriate hardware drive motors.
     * </p>
     * @param inleftFrontPower  Power to Left Front Wheel
     * @param inrightFrontPower Power to Right Front Wheel
     * @param inleftBackPower   Power to Left Back Wheel
     * @param inrightBackPower  Power to Right Back Wheel
     *
     * <br>
     */
    private void setDriveMotorPower(double inleftFrontPower, double inrightFrontPower, double inleftBackPower, double inrightBackPower) {

        // Send calculated power to wheels
        leftFrontDrive.setPower(inleftFrontPower);
        rightFrontDrive.setPower(inrightFrontPower);
        leftBackDrive.setPower(inleftBackPower);
        rightBackDrive.setPower(inrightBackPower);
    }

    /**
     * <h2>Drivetrain Method: setDriveMotorRunMode</h2>
     * <hr>
     * <b>Author:</b> {@value utilRobotConstants.About#COMMENT_AUTHOR_NAME}<br>
     * <b>Season:</b> {@value utilRobotConstants.About#COMMENT_SEASON_PERIOD}<br>
     * <hr>
     * <p>
     * Set the run mode for each drive motor.
     * </p>
     *
     * @param inRunMode DcMotor.RunMode - Set the run mode for each drive motor
     *
     */
    public void setDriveMotorRunMode(DcMotor.RunMode inRunMode) {
        for (DcMotorEx itemMotor: listMotorsDrivetrain) {
            itemMotor.setMode(inRunMode);
        }
    }

    /**
     * <h2>Drivetrain Method: setDriveMotorZeroPowerBehavior</h2>
     * <hr>
     * <b>Author:</b> {@value utilRobotConstants.About#COMMENT_AUTHOR_NAME}<br>
     * <b>Season:</b> {@value utilRobotConstants.About#COMMENT_SEASON_PERIOD}<br>
     * <hr>
     * <p>
     * Set the 'Zero Behavior' for each drive motor. Brake/Coast
     * </p>
     *
     * @param inZeroPowerBehavior DcMotor.ZeroPowerBehavior - Set the Zero Power behavior for each drive motor(s)
     *
     */
    public void setDriveMotorZeroPowerBehavior(DcMotor.ZeroPowerBehavior inZeroPowerBehavior) {
        for (DcMotorEx itemMotor : listMotorsDrivetrain) {
            itemMotor.setZeroPowerBehavior(inZeroPowerBehavior);
        }
    }

}
