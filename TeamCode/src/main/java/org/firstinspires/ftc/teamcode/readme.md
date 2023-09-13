## Techno Trojan Training - Drivetrain (Mecanum)

Welcome!

The Techno Trojan Training series provides a series of program repositories that provide 
working examples for training new developers. These public modules will be updated and maintained 
as the FIRST FTC framework is updated.

## Installing

Documentation coming soon

### Mode: Teleop - 'TT: Drivetrain Mecanum'

#### Controls
##### Gamepad1 = Main Driver
-- Robot Movement
-- -- Axis (left_stick_x, left_stick_y): Drive
-- -- Axis (right_stick_x): Rotate
-- -- Y: Set Output Speed to Med
-- -- A: Set Output Speed to Low

-- Robot Movement - Function(s) not in place!
-- -- X: 180 spin

-- Override Settings
-- -- D-Pad Up + X: Reset Heading Override (and Raw)

-- Not in place!
-- -- Back: Switch between Field-Centric and Robot-Centric drive

##### Gamepad2 = Co-Driver
NA


If the hub containing the IMU you are using is mounted so that the "REV" logo does
not face up, remap the IMU axes so that the z-axis points upward (normal to the floor.)

             | +Z axis
             |
             |
             |
      _______|_____________     +Y axis (roll)
     /       |_____________/|__________
    /   REV / EXPANSION   //
   /       / HUB         //
  /_______/_____________//
 |_______/_____________|/
        /
       / +X axis (pitch)

 This diagram is derived from the axes in section 3.4 https://www.bosch-sensortec.com/media/boschsensortec/downloads/datasheets/bst-bno055-ds000.pdf
 and the placement of the dot/orientation from https://docs.revrobotics.com/rev-control-system/control-system-overview/dimensions#imu-location


## Information

Documentation coming soon

