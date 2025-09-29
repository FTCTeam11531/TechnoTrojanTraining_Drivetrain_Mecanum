package org.firstinspires.ftc.teamcode.opmode.teleop;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.system.DrivetrainMecanum;
import org.firstinspires.ftc.teamcode.utility.RobotConstants;

import java.util.Locale;

@TeleOp(name="Driver Control", group="_main")
public class DriverControl extends LinearOpMode {

    // -------------------------------------------------
    // Misc - OpMode Variables
    // -------------------------------------------------
    ElapsedTime opModeRunTime = new ElapsedTime();

    // Drivetrain
    DrivetrainMecanum drivetrainMecanum = new DrivetrainMecanum(this);

    @Override
    public void runOpMode() throws InterruptedException {

        // ------------------------------
        // Initialize System(s)
        // ------------------------------

        // Drivetraim
        drivetrainMecanum.init();

        // Teleop Drive Mode
        drivetrainMecanum.setDriveMotorRunMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        // ------------------------------------------------------------
        // Variables for OpMode
        // ------------------------------------------------------------
        double inputAxial, inputLateral, inputYaw, restrictionExtensionMax;

        // Clear all telemetry
        telemetry.clearAll();

        // Repeat while opmode is in init mode (disable if using waitforstart)
        while (opModeInInit() && !isStopRequested()) {

            // ------------------------------------------------------------
            // Send telemetry message to signify robot completed initialization and waiting to start;
            // ------------------------------------------------------------
            telemetry.addData("-", "------------------------------------");
            telemetry.addData("-", "All Systems Ready - Waiting to Start");
            telemetry.addData("-","--------------------------------------");
            telemetry.addData("run time", "%.1f seconds", opModeRunTime.seconds());
            telemetry.addData("-","--------------------------------------");
            telemetry.addData("drivetrain", String.format(Locale.US,"{mode: %s, speed: %s}", drivetrainMecanum.getDrivetrainMode().getLabel(), drivetrainMecanum.getDrivetrainSpeed().getLabel()));
            telemetry.addData("-","--------------------------------------");

            // Show joystick information
            telemetry.addData("-","--------------------------------------");
            telemetry.addData("-","-- Controller Input");
            telemetry.addData("-","--------------------------------------");
            telemetry.addData("main", String.format(Locale.US,"{left X: %.3f, Left Y: %.3f, Right X: %.3f, Right Y: %.3f}", gamepad1.left_stick_x, gamepad1.left_stick_y, gamepad1.right_stick_x, gamepad1.right_stick_y));
            telemetry.addData("alt", String.format(Locale.US,"{left X: %.3f, Left Y: %.3f, Right X: %.3f, Right Y: %.3f}", gamepad2.left_stick_x, gamepad2.left_stick_y, gamepad2.right_stick_x, gamepad2.right_stick_y));
            telemetry.addData("trigger", String.format(Locale.US,"{left: %.3f, Right: %.3f}", gamepad2.left_trigger, gamepad2.right_trigger));

            // Show imu / odometry information
            telemetry.addData("-","--------------------------------------");
            telemetry.addData("-","-- Inertia Measurement Unit");
            telemetry.addData("-","--------------------------------------");
            telemetry.addData("imu", drivetrainMecanum.getImuStatus());
//            telemetry.addData("heading", String.format(Locale.US,"{raw: %.3f, adj: %.3f}", sysDrivetrain.getRobotHeadingRaw(), sysDrivetrain.getRobotHeadingAdj()));
//            telemetry.addData("position", sysDrivetrain.getImuPositionDetail());
//            telemetry.addData("velocity", sysDrivetrain.getImuVelocityDetail());


            // Show Vision
            telemetry.addData("-","--------------------------------------");
            telemetry.addData("-","-- Vision");
            telemetry.addData("-","--------------------------------------");
//            telemetry.addData("alliance", String.format(Locale.US,"{color: %s, red: %d, blue: %d, green: %d}", sysVision.getAllianceColor(), sysVision.getAllianceColorValueRed(), sysVision.getAllianceColorValueBlue(), sysVision.getAllianceColorValueGreen()));

            // ------------------------------------------------------------
            // - send telemetry to driver hub
            // ------------------------------------------------------------
            telemetry.update();
            idle();
        }

        // Wait for Start state (from driver station) - (disable if using an init loop)
//        waitForStart();

        // Reset runtime timer
        opModeRunTime.reset();

        // Clear all telemetry
        telemetry.clearAll();

        // Robot OpMode Loop
        while (opModeIsActive() && !isStopRequested()) {

            // ------------------------------------------------------------
            // Controls
            // ------------------------------------------------------------
            // Gamepad1 = Main Driver
            // ------------------------------------------------------------
            // -- Robot Movement
            // -- -- Axis (left_stick_x, left_stick_y): Drive
            // -- -- Axis (right_stick_x): Rotate
            //

            // ------------------------------------------------------------
            // Drivetrain
            // ------------------------------------------------------------
            // Assign gamepad control to motion in relation to:
            // -- gamepad input, direction
            // -- robot orientation to field
            // -- installed direction of control hub
            // -- orientation of drivetrain/motors
            inputYaw =  (gamepad1.right_stick_x);
            inputAxial = -(gamepad1.left_stick_y);
            inputLateral = (gamepad1.left_stick_x);

            // Update Odometry Reading(s)
//            sysDrivetrain.updateOdometry();

            // Endgame Notification
//            if(opModeRunTime.time() >= RobotConstants.CommonSettings.GameSettings.kEndgameStartTime && opModeRunTime.time() <= RobotConstants.CommonSettings.GameSettings.kEndgameEndTime) {
//                sysLighting.setLightPattern(RobotConstants.Lighting.Pattern.Default.kEndgame);
//            }
//            else if(opModeRunTime.time() >= RobotConstants.CommonSettings.GameSettings.kEndgameEndTime) {
//                sysLighting.setLightPattern(RobotConstants.Lighting.Pattern.Default.kEnd);
//            }

            // Drivetrain Type determined by 'Drivetrain Mode' enumeration selection (Default to Field Centric)
//            if(sysDrivetrain.getDrivetrainMode().equals(SysDrivetrain.DrivetrainMode.ROBOT_CENTRIC)) {
//
//                // Set Robot Centric Drivetrain
//                sysDrivetrain.driveMecanum(inputAxial, inputLateral, inputYaw, sysDrivetrain.getDrivetrainOutputPower().getValue());
//            }
//            else {

            // Set Field Centric Drivetrain
            drivetrainMecanum.driveFieldCentric(inputAxial, inputLateral, inputYaw, RobotConstants.Drivetrain.Configuration.kMotorOutputPowerMedium);
//            }

            // ------------------------------------
            // Drivetrain Speed Options
            // ------------------------------------
//            if(gamepad1.right_bumper) {
//                sysDrivetrain.setDrivetrainOutputPower(SysDrivetrainPinpoint.DrivetrainSpeed.MEDIUM);
//            }
//
//            if(!gamepad1.right_bumper) {
//                sysDrivetrain.setDrivetrainOutputPower(SysDrivetrainPinpoint.DrivetrainSpeed.LOW);
//            }

//            if(gamepad1.dpad_up) {
//                sysDrivetrain.setDrivetrainOutputPower(SysDrivetrainPinpoint.DrivetrainSpeed.HIGH);
//            }
//
//            if(gamepad1.dpad_down) {
//                sysDrivetrain.setDrivetrainOutputPower(SysDrivetrainPinpoint.DrivetrainSpeed.LOW);
//            }
//
//            if(gamepad1.dpad_left) {
//                sysDrivetrain.setDrivetrainOutputPower(SysDrivetrainPinpoint.DrivetrainSpeed.MEDIUM);
//            }
//
//            if(gamepad1.dpad_right) {
//                sysDrivetrain.setDrivetrainOutputPower(SysDrivetrainPinpoint.DrivetrainSpeed.SNAIL);
//            }

            // ------------------------------------
            // Drivetrain Mode Options
            // ------------------------------------
//            if(gamepad1.start && gamepad1.dpad_up) {
//                sysDrivetrain.setDrivetrainMode(SysDrivetrainPinpoint.DrivetrainMode.FIELD_CENTRIC);
//            }

//            if(gamepad1.start && gamepad1.dpad_down) {
//                sysDrivetrain.setDrivetrainMode(SysDrivetrainPinpoint.DrivetrainMode.ROBOT_CENTRIC);
//            }


            // ------------------------------------
            // Override
            // ------------------------------------
            if(gamepad1.start && gamepad1.y) {

                // Reset the Robot Heading (normally done on init of Drivetrain system)
                drivetrainMecanum.resetZeroRobotHeading();
            }

            // ------------------------------------
            // Driver Hub Feedback
            // ------------------------------------
            telemetry.addData("-","--------------------------------------");
            telemetry.addData("-","-- Teleop - Main");
            telemetry.addData("-","--------------------------------------");
            telemetry.addData("run time", "%.1f seconds", opModeRunTime.seconds());
            telemetry.addData("-","--------------------------------------");
//            telemetry.addData("drivetrain", String.format(Locale.US,"{mode: %s, speed: %s}", sysDrivetrain.getDrivetrainMode().getLabel(), sysDrivetrain.getDrivetrainOutputPower().getLabel()));
            telemetry.addData("-","--------------------------------------");
//            telemetry.addData("light mode", sysLighting.getLightPatternCurrent().toString());
            telemetry.addData("-","--------------------------------------");

            // Show joystick information
            telemetry.addData("-","--------------------------------------");
            telemetry.addData("-","-- Controller Input");
            telemetry.addData("-","--------------------------------------");
            telemetry.addData("main", String.format(Locale.US,"{left X: %.3f, Left Y: %.3f, Right X: %.3f, Right Y: %.3f}", gamepad1.left_stick_x, gamepad1.left_stick_y, gamepad1.right_stick_x, gamepad1.right_stick_y));
            telemetry.addData("alt", String.format(Locale.US,"{left X: %.3f, Left Y: %.3f, Right X: %.3f, Right Y: %.3f}", gamepad2.left_stick_x, gamepad2.left_stick_y, gamepad2.right_stick_x, gamepad2.right_stick_y));
            telemetry.addData("trigger", String.format(Locale.US,"{left: %.3f, Right: %.3f}", gamepad2.left_trigger, gamepad2.right_trigger));

            // Show imu / odometry information
            telemetry.addData("-","--------------------------------------");
            telemetry.addData("-","-- Inertia Measurement Unit");
            telemetry.addData("-","--------------------------------------");
//            telemetry.addData("imu", sysDrivetrain.getImuStatus());
            telemetry.addData("heading", String.format(Locale.US,"{raw: %.3f, adj: %.3f}", drivetrainMecanum.getRobotHeadingRaw(), drivetrainMecanum.getRobotHeadingAdj()));
//            telemetry.addData("position", sysDrivetrain.getImuPositionDetail());
//            telemetry.addData("velocity", sysDrivetrain.getImuVelocityDetail());


            // ------------------------------------------------------------
            // - send telemetry to driver hub
            // ------------------------------------------------------------
            telemetry.update();
        }


    }
}
