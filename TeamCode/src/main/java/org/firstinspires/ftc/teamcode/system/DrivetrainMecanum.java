package org.firstinspires.ftc.teamcode.system;

import com.qualcomm.hardware.bosch.BHI260IMU;
//import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.hardware.bosch.BNO055IMU;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.MotorConfigurationType;
import com.qualcomm.robotcore.util.Range;

import java.util.Arrays;
import java.util.List;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AngularVelocity;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;
import org.firstinspires.ftc.teamcode.utility.RobotConstants;

public class DrivetrainMecanum {

    // Enum: Drive mode
    public enum DrivetrainMode {

        FIELD_CENTRIC {

            // Label for setting
            @Override
            public String getLabel() { return "Field Centric"; }

        },
        ROBOT_CENTRIC {

            // Label for setting
            @Override
            public String getLabel() { return "Robot Centric"; }

        };

        public abstract String getLabel();
    }

    // Enum: Drive Speed
    public enum DrivetrainSpeed {

        HIGH {

            // Value for setting
            @Override
            public double getValue() { return RobotConstants.Drivetrain.Configuration.kMotorOutputPowerHigh; }

            // Label for setting
            @Override
            public String getLabel() { return "High"; }

        },
        MEDIUM {

            // Value for setting
            @Override
            public double getValue() { return RobotConstants.Drivetrain.Configuration.kMotorOutputPowerMedium; }

            // Label for setting
            @Override
            public String getLabel() { return "Medium"; }

        },
        LOW {

            // Value for setting
            @Override
            public double getValue() { return RobotConstants.Drivetrain.Configuration.kMotorOutputPowerLow; }

            // Label for setting
            @Override
            public String getLabel() { return "Low"; }

        },
        SNAIL {

            // Value for setting
            @Override
            public double getValue() { return RobotConstants.Drivetrain.Configuration.kMotorOutputPowerSnail; }

            // Label for setting
            @Override
            public String getLabel() { return "Snail"; }

        };

        public abstract double getValue();
        public abstract String getLabel();
    }

    // System Op Mode
    private LinearOpMode opMode;

    // Drivetrain Settings
    private DrivetrainMode drivetrainMode = DrivetrainMode.FIELD_CENTRIC;
    private DrivetrainSpeed drivetrainSpeed = DrivetrainSpeed.MEDIUM;

    // System HardwareMap
//    private HardwareMap hardwareMap;

    // Define Hardware for subsystem
    // ----------------------------------------

    // Motor(s)
    private DcMotorEx driveFrontLeft, driveFrontRight, driveRearLeft, driveRearRight;
    private List<DcMotorEx> listMotorDrivetrain;

    // Define Hardware - Control Hub
    private RevHubOrientationOnRobot controlHubOrientation = null;
    private RevHubOrientationOnRobot.LogoFacingDirection controlHubLogoDirection = RobotConstants.HardwareConfiguration.kControlHubLogoDirection;
    private RevHubOrientationOnRobot.UsbFacingDirection controlHubUsbDirection = RobotConstants.HardwareConfiguration.kControlHubUsbDirection;

    // Define Hardware - IMU
    private IMU imuDevice = null;
    private IMU.Parameters imuParameters;

    private double trackHeadingRobot, trackHeadingOffset, trackHeadingError;

    // Constructor
    public DrivetrainMecanum(LinearOpMode opMode) {
        this.opMode = opMode;
    }

    // Init method - initialization steps for subsystem
    public void init() {

        // Telemetry - Initialize - Start
        opMode.telemetry.addData(">", "------------------------------------");
        opMode.telemetry.addData(">", "System: Drivetrain");
        opMode.telemetry.addData(">", "------------------------------------");
        opMode.telemetry.update();

        // Define and Initialize Motor(s)
        driveFrontLeft = opMode.hardwareMap.get(DcMotorEx.class, RobotConstants.HardwareConfiguration.kLabelDrivetrainMotorLeftFront);
        driveFrontRight = opMode.hardwareMap.get(DcMotorEx.class, RobotConstants.HardwareConfiguration.kLabelDrivetrainMotorRightFront);
        driveRearLeft = opMode.hardwareMap.get(DcMotorEx.class, RobotConstants.HardwareConfiguration.kLabelDrivetrainMotorLeftBack);
        driveRearRight = opMode.hardwareMap.get(DcMotorEx.class, RobotConstants.HardwareConfiguration.kLabelDrivetrainMotorRightBack);

        // Add Motors to Array for like configuration(s)
        listMotorDrivetrain = Arrays.asList(driveFrontLeft, driveFrontRight, driveRearLeft, driveRearRight);

        // Set common configuration for drive motor(s)
        for (DcMotorEx itemMotor : listMotorDrivetrain) {

            // clone motor configuration
            MotorConfigurationType motorConfigurationType = itemMotor.getMotorType().clone();

            // Set motor configuration properties
            motorConfigurationType.setAchieveableMaxRPMFraction(RobotConstants.Drivetrain.Configuration.kMotorAchievableMaxRpmFraction);

            // Write out motor configuration to motor
            itemMotor.setMotorType(motorConfigurationType);
        }


        // Set Zero Power mode for drivetrain
        setDriveMotorZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);

        // Set Non-common motor configuration(s)
        driveFrontLeft.setDirection(DcMotorEx.Direction.FORWARD);
        driveFrontRight.setDirection(DcMotorEx.Direction.REVERSE);
        driveRearLeft.setDirection(DcMotorEx.Direction.FORWARD);
        driveRearRight.setDirection(DcMotorEx.Direction.REVERSE);


        // Set motor mode(s)


        // Define IMU Device
        imuDevice = opMode.hardwareMap.get(IMU.class, RobotConstants.HardwareConfiguration.kLabelDrivetrainIMUDeviceAlt);

        // Initialize the IMU unit

        // Initialize the IMU board/unit on the Rev Control Hub
        controlHubOrientation = new RevHubOrientationOnRobot(controlHubLogoDirection, controlHubUsbDirection);
        imuParameters = new IMU.Parameters(controlHubOrientation);

        imuDevice.initialize(imuParameters);

        // Reset Drive Motor Encoder(s)
        setDriveMotorRunMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
        resetRobotHeading();

        // Telemetry - Initialize - End
        opMode.telemetry.addData(">", "------------------------------------");
        opMode.telemetry.addData(">", "System: Drivetrain (Initialized)");
        opMode.telemetry.addData(">", "------------------------------------");
        opMode.telemetry.update();

    }


    // ----------------------------------------------
    // Action Methods
    // ----------------------------------------------


    /**
     * <h2>Drivetrain Method: driveMecanum</h2>
     * <hr>
     * <b>Author:</b> {@value RobotConstants.About#kAboutAuthorName}<br>
     * <b>Season:</b> {@value RobotConstants.About#kAboutSeasonPeriod}<br>
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
    public void driveRobotCentric(double inAxial, double inLateral, double inYaw, double inMaxOutputPowerPercent) {

        double modMaintainMotorRatio;

        double inputAxial   = (inAxial * inMaxOutputPowerPercent);  // Note: pushing stick forward gives negative value
        double inputLateral = (inLateral * inMaxOutputPowerPercent) * RobotConstants.Drivetrain.Configuration.kMotorLateralMovementStrafingCorrection; // Mod to even out strafing
        double inputYaw     = (inYaw * inMaxOutputPowerPercent);

        // Normalize the values so no wheel power exceeds 100%
        // This ensures that the robot maintains the desired motion.
        modMaintainMotorRatio = Math.max(Math.abs(inputAxial) + Math.abs(inputLateral) + Math.abs(inputYaw), RobotConstants.Drivetrain.Configuration.kMotorOutputPowerMax);

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
     * <b>Author:</b> {@value RobotConstants.About#kAboutAuthorName}<br>
     * <b>Season:</b> {@value RobotConstants.About#kAboutSeasonPeriod}<br>
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
    public void driveFieldCentric(double inAxial, double inLateral, double inYaw, double inMaxOutputPowerPercent) {

        double modMaintainMotorRatio;

        double inputAxial   = (inAxial * inMaxOutputPowerPercent);  // Note: pushing stick forward gives negative value
        double inputLateral = (inLateral * inMaxOutputPowerPercent) * RobotConstants.Drivetrain.Configuration.kMotorLateralMovementStrafingCorrection; // Mod to even out strafing
        double inputYaw     = (inYaw * inMaxOutputPowerPercent);

        // Get heading value from the IMU
        double botHeading = getRobotHeadingAdj();

        // Adjust the lateral and axial movements based on heading
        double adjLateral = inputLateral * Math.cos(botHeading) - inputAxial * Math.sin(botHeading);
        double adjAxial = inputLateral * Math.sin(botHeading) + inputAxial * Math.cos(botHeading);

        // Normalize the values so no wheel power exceeds 100%
        // This ensures that the robot maintains the desired motion.
        modMaintainMotorRatio = Math.max(Math.abs(inputAxial) + Math.abs(inputLateral) + Math.abs(inputYaw), RobotConstants.Drivetrain.Configuration.kMotorOutputPowerMax);

        // Combine the joystick requests for each axis-motion to determine each wheel's power.
        // Set up a variable for each drive wheel to save the power level for telemetry.
        double leftFrontPower  = (adjAxial + adjLateral + inputYaw) / modMaintainMotorRatio;
        double rightFrontPower = (adjAxial - adjLateral - inputYaw) / modMaintainMotorRatio;
        double leftBackPower   = (adjAxial - adjLateral + inputYaw) / modMaintainMotorRatio;
        double rightBackPower  = (adjAxial + adjLateral - inputYaw) / modMaintainMotorRatio;

        // Use existing function to drive both wheels.
        setDriveMotorPower(leftFrontPower, rightFrontPower, leftBackPower, rightBackPower);
    }

//    public void updateOdometry() {
//        pinpoint.update();
//    }

    public void resetRobotHeading() {

        // Set the Heading Offset to the IMU raw heading
        trackHeadingOffset = getRobotHeadingRaw();

        // Reset the Robot Heading to Zero
        trackHeadingRobot = 0;
    }

    public void resetZeroRobotHeading() {

        // Set the Heading Offset to the IMU raw heading
        imuDevice.resetYaw();
        RobotConstants.Drivetrain.Odometry.Transition.setImuTransitionAdjustment(0);
    }

    // ----------------------------------------------
    // Get Methods
    // ----------------------------------------------
    public double getRobotHeadingRaw() {
        // Variable for output heading value
        double outRobotHeadingValue;

        // Get heading value from the IMU
        // Read inverse IMU heading, as the IMU heading is CW positive
        outRobotHeadingValue = -(imuDevice.getRobotYawPitchRollAngles().getYaw(AngleUnit.RADIANS));

        // Should the IMU heading be inversed? Does it matter?
        // Will need to view the heading readout on the driver hub
        return outRobotHeadingValue;
    }

    public double getRobotHeadingAdj() {

        // Variable for output heading value
        double outRobotHeadingValue;


        // TODO: Check on input directional inversion... may not need the inversion here!

        // Get heading value from the IMU
        // Read inverse IMU heading, as the IMU heading is CW positive
        outRobotHeadingValue = getRobotHeadingRaw() + RobotConstants.Drivetrain.Odometry.Transition.getImuTransitionAdjustment();

        // Should the IMU heading be inversed? Does it matter?
        // Will need to view the heading readout on the driver hub

        //imuUnit.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES).firstAngle

        return outRobotHeadingValue;
    }

    public double getSteeringCorrection(double inTargetHeader, double inProportionalGain) {

        // Get robot header by subtracking the offset from the heading
//        trackHeadingRobot = imuUnit.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES).firstAngle;
        trackHeadingRobot = imuDevice.getRobotOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES).firstAngle;

        // Determine the heading current error
        trackHeadingError = inTargetHeader - trackHeadingRobot;

        // Normalize the error to be within +/- 180 degrees
//        while (trackHeadingError > 180) trackHeadingError -= 360;
//        while (trackHeadingError <= -180) trackHeadingError += 360;

        return Range.clip(trackHeadingError * inProportionalGain, -1, 1);
    }


    public YawPitchRollAngles getRobotAngles() {
        return imuDevice.getRobotYawPitchRollAngles();
    }

    public AngularVelocity getRobotAngularVelocity() {
        return imuDevice.getRobotAngularVelocity(AngleUnit.DEGREES);
    }

    public String getImuStatus() {
        return imuDevice.getDeviceName();
    }

    public DrivetrainMode getDrivetrainMode() {
        return drivetrainMode;
    }

    public DrivetrainSpeed getDrivetrainSpeed() {
        return drivetrainSpeed;
    }


    // ----------------------------------------------
    // Set Methods
    // ----------------------------------------------

    public void setDriveMotorRunMode(DcMotorEx.RunMode inRunMode) {
        for (DcMotorEx itemMotor: listMotorDrivetrain) {
            itemMotor.setMode(inRunMode);
        }
    }

    public void setDriveMotorZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior inZeroPowerBehavior) {
        for (DcMotorEx itemMotor : listMotorDrivetrain) {
            itemMotor.setZeroPowerBehavior(inZeroPowerBehavior);
        }
    }

    private void setDriveMotorPower(double powerLeftFront, double powerRightFront, double powerLeftBack, double powerRightBack) {

        // Send calculated power to wheels
        driveFrontLeft.setPower(powerLeftFront);
        driveFrontRight.setPower(powerRightFront);
        driveRearLeft.setPower(powerLeftBack);
        driveRearRight.setPower(powerRightBack);
    }

    public void setDrivetrainMode(DrivetrainMode newDrivetrainMode) {
        drivetrainMode = newDrivetrainMode;
    }

    public void setDrivetrainSpeed(DrivetrainSpeed newDrivetrainSpeed) {
        drivetrainSpeed = newDrivetrainSpeed;
    }

}
