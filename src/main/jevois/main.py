import libjevois as jevois
import cv2
import numpy as np
import math  # for cos, sin, etc
import time

INCHES_TO_METRES = 0.0254
GOAL_HEIGHT = 98.25 * INCHES_TO_METRES  # metres to the center of goal
GOAL_WIDTH = 39.25 * INCHES_TO_METRES  # metres

SCREEN_WIDTH = 320
SCREEN_HEIGHT = 240  # pixels

CAMERA_HORI_FOV = 65  # horizontal
CAMERA_VERT_FOV = CAMERA_HORI_FOV * (3 / 4)


def deg_to_rad(degrees):
    return degrees * (math.pi / 180)


# converts radians to degrees
def rad_to_deg(radians):
    return radians * (180 / math.pi)


# sine function which uses degrees
def sin(degrees):
    return math.sin(deg_to_rad(degrees))


# cosine function which uses degrees
def cos(degrees):
    return math.cos(deg_to_rad(degrees))


# tan function which uses degrees
def tan(degrees):
    return math.tan(deg_to_rad(degrees))


# inverse sine function which returns degrees
def asin(ratio):
    return rad_to_deg(math.asin(ratio))


# inverse cosine function which returns degrees
def acos(ratio):
    return rad_to_deg(math.acos(ratio))


# inverse tan function which returns degrees
def atan(degrees):
    return rad_to_deg(math.atan(degrees))


def cal_point_distance(p, q):
    x = math.sqrt((p[0] - q[0]) ** 2 + (p[1] - q[1]) ** 2)
    return x


# calculates the angle in a triangle of known lengths
# adj1 and adj2 are the adjacent sides to the angle
# opposite is the opposite angle to the the desired angle
def cal_cosine_rule_deg(adj1, adj2, opposite):
    adj1_sqrd = math.pow(adj1, 2)
    adj2_sqrd = math.pow(adj2, 2)
    opposite_sqrd = math.pow(opposite, 2)
    if abs((adj1_sqrd + adj2_sqrd - opposite_sqrd) / (2 * adj1 * adj2)) > 1:
        # print "error in cal_cosine_rule_deg() opposite_sqd = %f" %opposite_sqrd
        return 0
    return acos((adj1_sqrd + adj2_sqrd - opposite_sqrd) / (2 * adj1 * adj2))


class Corner:
    def __init__(self):
        self.xy = []
        self.score = -10000

    def update_score(self, X, Y, score):
        if score > self.score:
            self.xy = [X, Y]
            self.score = score


def cal_corners(contour):
    TL_corner = Corner()
    TR_corner = Corner()
    BL_corner = Corner()
    BR_corner = Corner()
    for point in contour:
        x = point[0][0]  # +ve is more right
        y = point[0][1]  # +ve is more down
        TL_corner.update_score(x, y, -x - y)
        TR_corner.update_score(x, y, x - y)
        BL_corner.update_score(x, y, -x + y)
        BR_corner.update_score(x, y, x + y)
    TL = TL_corner.xy
    TR = TR_corner.xy
    BL = BL_corner.xy
    BR = BR_corner.xy
    return TL, TR, BL, BR


# Goal distance calculations


def cal_distance(center_y, cameraPitch, cameraHeight):
    # angle between the bottom of FOV and horizontal
    lowerAngle = cameraPitch - CAMERA_VERT_FOV / 2

    # angle between the line to the goal and the horizontal
    goalAngle = lowerAngle + (
        ((SCREEN_HEIGHT - center_y) / SCREEN_HEIGHT) * CAMERA_VERT_FOV
    )

    distance = (GOAL_HEIGHT - cameraHeight) / tan(goalAngle)
    return distance


def cal_angle(x):  # based on  FOV
    angle = ((x - (SCREEN_WIDTH / 2)) / (SCREEN_WIDTH / 2)) * (CAMERA_HORI_FOV / 2)
    return angle


def cal_goal_center_direct_distance(distance, cameraHeight):
    opposite = GOAL_HEIGHT - cameraHeight
    hypotenuse = math.sqrt(pow(opposite, 2) + pow(distance, 2))
    return hypotenuse


def cal_goal_skew(TL, TR, BL, BR, center_direct_distance, cameraPitch, cameraHeight):
    # center_direct_distance is the hypotenuse
    HALF_WIDTH_GOAL = GOAL_WIDTH / 2

    left_side_distance = cal_distance(TL[1], cameraPitch, cameraHeight)
    left_acute_angle = cal_cosine_rule_deg(
        center_direct_distance, HALF_WIDTH_GOAL, left_side_distance
    )

    right_side_distance = cal_distance(TR[1], cameraPitch, cameraHeight)
    right_acute_angle = cal_cosine_rule_deg(
        center_direct_distance, HALF_WIDTH_GOAL, right_side_distance
    )

    avg_angle = (left_acute_angle + (180 - right_acute_angle)) / 2

    return 90 - avg_angle


class FirstPython:
    # ###################################################################################################
    ## Constructor
    def __init__(self):
        # HSV color range to use:
        #
        # H: 0=red/do not use because of wraparound, 30=yellow, 45=light green, 60=green, 75=green cyan, 90=cyan,
        #      105=light blue, 120=blue, 135=purple, 150=pink
        # S: 0 for unsaturated (whitish discolored object) to 255 for fully saturated (solid color)
        # V: 0 for dark to 255 for maximally bright

        self.HSVmin = np.array([65, 60, 62], dtype=np.uint8)
        self.HSVmax = np.array([97, 255, 252], dtype=np.uint8)

        # Other processing parameters:
        self.epsilon = 0.015  # Shape smoothing factor (higher for smoother)
        self.hullarea = (3 * 3, 30 * 30)  # Range of object area (in pixels) to track
        self.hullfill = 50  # Max fill ratio of the convex hull (percent)
        self.ethresh = 1500  # Shape error threshold (lower is stricter for exact shape)
        self.margin = 5  # Margin from from frame borders (pixels)
        self.img_rotate_angle = 0  # angle the outimg rotates in degrees, positive value is counter clockwise
        self.scale = 1.0  # isotropic scale factor
        self.cameraHeight = 0.4  # metres above ground
        self.cameraPitch = 25  # degrees from horizontal

        # Instantiate a JeVois Timer to measure our processing framerate:
        self.timer = jevois.Timer("FirstPython", 100, jevois.LOG_INFO)

        # CAUTION: The constructor is a time-critical code section. Taking too long here could upset USB timings and/or
        # video capture software running on the host computer. Only init the strict minimum here, and do not use OpenCV,
        # read files, etc

    # ###################################################################################################
    ## Parse a serial command forwarded to us by the JeVois Engine, return a string
    def parseSerial(self, str):
        jevois.LINFO("parseSerial received command [{}]".format(str))
        parts = str.split()
        try:
            if parts[0] == "setHSVMin":
                self.HSVmin, response = self.parseHSVValues(parts)
                return response
            if parts[0] == "setHSVMax":
                self.HSVmax, response = self.parseHSVValues(parts)
                return response
            if parts[0] == "position":
                if len(parts) != 4:
                    raise Exception(
                        "Insufficient number of parameters for camera position, expected four"
                    )
                self.cameraHeight = int(parts[1])
                self.cameraPitch = int(parts[2])
                self.img_rotate_angle = int(parts[3])
                return "Camera position set to: Height: {}metres, Pitch: {}degrees, Roll: {}degrees".format(
                    self.cameraHeight, self.cameraPitch, self.img_rotate_angle
                )

        except Exception as e:
            return "ERR {}".format(e)
        return "ERR Unsupported command {}".format(parts[0])

    def parseHSVValues(self, parts):
        if len(parts) != 4:
            raise Exception(
                "Insufficient number of parameters for setHSV{Min|Max}, expected four"
            )
        h = int(parts[1])
        s = int(parts[2])
        v = int(parts[3])
        return (
            np.array([h, s, v], dtype=np.uint8),
            "HSV min set to {} {} {}".format(h, s, v),
        )

    # ###################################################################################################
    ## Load camera calibration from JeVois share directory
    def loadCameraCalibration(self, w, h):
        cpf = "/jevois/share/camera/calibration{}x{}.yaml".format(w, h)
        fs = cv2.FileStorage(cpf, cv2.FILE_STORAGE_READ)
        if fs.isOpened():
            self.camMatrix = fs.getNode("camera_matrix").mat()
            self.distCoeffs = fs.getNode("distortion_coefficients").mat()
            jevois.LINFO("Loaded camera calibration from {}".format(cpf))
        else:
            jevois.LFATAL("Failed to read camera parameters from file [{}]".format(cpf))

    def thresholding(self, imgbgr):

        # Convert input image to HSV:
        imghsv = cv2.cvtColor(imgbgr, cv2.COLOR_BGR2HSV)

        # Isolate pixels inside our desired HSV range:
        imgth = cv2.inRange(imghsv, self.HSVmin, self.HSVmax)

        # Create structuring elements for morpho maths:
        if not hasattr(self, "erodeElement"):
            self.erodeElement = cv2.getStructuringElement(cv2.MORPH_RECT, (2, 2))
            self.dilateElement = cv2.getStructuringElement(cv2.MORPH_RECT, (2, 2))

        # Apply morphological operations to cleanup the image noise:
        imgth = cv2.erode(imgth, self.erodeElement)
        imgth = cv2.dilate(imgth, self.dilateElement)

        imgth = cv2.medianBlur(imgth, 3)

        return imgth

    # ###################################################################################################
    ## Detect objects within our HSV range
    # Do the following checks to ensure it's the correct shape:
    # Hull is quadrilateral
    # Number of edges / vertices
    # Angle of lines
    # Top corners further apart than bottom corners
    # Area
    # Fill

    def detect(self, imgbgr, outimg=None):
        maxn = 5  # max number of objects we will consider
        h, w = imgbgr.shape

        maskValues = "H={}-{} S={}-{} V={}-{} ".format(
            self.HSVmin[0],
            self.HSVmax[0],
            self.HSVmin[1],
            self.HSVmax[1],
            self.HSVmin[2],
            self.HSVmax[2],
        )

        # Detect objects by finding contours:
        contours, hierarchy = cv2.findContours(
            imgbgr, cv2.RETR_CCOMP, cv2.CHAIN_APPROX_SIMPLE
        )
        maskValues += "N={} ".format(len(contours))

        # Only consider the 5 biggest objects by area:
        contours = sorted(contours, key=cv2.contourArea, reverse=True)[:maxn]
        goalCriteria = ""
        found = False

        targets = []

        # Identify the "good" objects:
        for c in contours:

            # Keep track of our best detection so far:
            goalCriteria = ""

            # Compute contour area:
            area = cv2.contourArea(c, oriented=False)

            # Compute convex hull:
            rawhull = cv2.convexHull(c, clockwise=True)
            rawhullperi = cv2.arcLength(rawhull, closed=True)
            hull = cv2.approxPolyDP(
                rawhull, epsilon=self.epsilon * rawhullperi * 3.0, closed=True
            )

            # Is it the right shape?
            # if hull.shape != (4, 1, 2):
            #     continue  # 4 vertices for the rectangular convex outline (shows as a trapezoid)
            # commented as targets are too curved and this check is unreliable
            goalCriteria += "H"  # Hull is quadrilateral

            huarea = cv2.contourArea(hull, oriented=False)
            if huarea < self.hullarea[0] or huarea > self.hullarea[1]:
                continue
            goalCriteria += "A"  # Hull area ok

            hufill = area / huarea * 100.0
            if hufill < self.hullfill:
                continue
            goalCriteria += "F"  # Fill is ok

            # Check object shape:
            peri = cv2.arcLength(c, closed=True)
            approx = cv2.approxPolyDP(c, epsilon=0.015 * peri, closed=True)

            # Reject the shape if any of its vertices gets within the margin of the image bounds. This is to avoid
            # getting grossly incorrect 6D pose estimates as the shape starts getting truncated as it partially exits
            # the camera field of view:
            reject = False
            for v in c:
                if (
                    v[0, 0] < self.margin
                    or v[0, 0] >= w - self.margin
                    or v[0, 1] < self.margin
                    or v[0, 1] >= h - self.margin
                ):
                    reject = True
                    break

            if reject == True:
                continue
            goalCriteria += "M"  # Margin ok

            bx, by, bw, bh = cv2.boundingRect(c)
            ratio = float(bw) / bh  # expected ratio is 5.0/2 = 2.5
            # some could be 1.0/2
            if ratio > 3:
                continue
            if ratio < 0.5:
                continue
            goalCriteria += "R"  # ratio is good

            # This detection is a keeper:
            goalCriteria += " OK"
            targets.append(c)
            found = True

        distance = 0
        angle = 0
        skew = 0
        # If multiple vision tape pieces are found, store corners of leftmost and rightmost ones.
        # Left top left, left top right, left bottom left, left bottom right, right top left, etc...
        LTL = LTR = LBL = LBR = RTL = RTR = RBL = RBR = []
        if found:
            # sort contour by x pos
            targets.sort(key=lambda x: x[0][0][0])
            # get middle x and y pos
            LTL, LTR, LBL, LBR = cal_corners(targets[0])
            RTL, RTR, RBL, RBR = cal_corners(targets[-1])
            mx = int((LTL[0] + RTR[0]) / 2)
            my = 0
            for i in range(len(targets)):
                TL, TR, BL, BR = cal_corners(targets[i])
                my += BL[1] - TL[1]
            my = int(my / len(targets))
            distance = cal_distance(my, self.cameraPitch, self.cameraHeight)
            center_direct_distance = cal_goal_center_direct_distance(
                distance, self.cameraHeight
            )
            skew = 0  # cal_goal_skew(TL,TR,BL,BR, center_direct_distance, self.cameraPitch, self.cameraHeight)
            angle = cal_angle(mx)
            goalCriteria += " gdist=" + str(round(distance, 3))
            goalCriteria += " angle=" + str(round(angle, 3))
            goalCriteria += " skew=" + str(round(skew, 3))
            goalCriteria += " found=" + str(found)

        # Display any results requested by the users:
        if outimg is not None and outimg.valid():
            if outimg.width == w * 2:
                jevois.pasteGreyToYUYV(imgbgr, outimg, w, 0)
            jevois.writeText(
                outimg,
                maskValues + goalCriteria,
                3,
                h + 1,
                jevois.YUYV.White,
                jevois.Font.Font6x10,
            )
            # Draw corners and center of goal:
            if found:
                self.drawDetections(outimg, LTL, RTR, LBL, RBR)

        return found, distance, angle, skew

    # ###################################################################################################
    ## Send serial messages, one per object
    def sendAllSerial(self, imageAge, found, distance, angle, skew):
        jevois.sendSerial(
            "D3 {} {} {} {} {} FIRST".format(imageAge, found, distance, angle, skew)
        )

    # ###################################################################################################
    ## Draw corners and center of goal
    def drawDetections(self, outimg, TL, TR, BL, BR):
        try:
            jevois.drawLine(
                outimg,
                int(TL[0]),
                int(TL[1]),
                int(TR[0]),
                int(TR[1]),
                1,
                jevois.YUYV.LightPink,
            )
            jevois.drawLine(
                outimg,
                int(TR[0]),
                int(TR[1]),
                int(BR[0]),
                int(BR[1]),
                1,
                jevois.YUYV.LightPink,
            )
            jevois.drawLine(
                outimg,
                int(BR[0]),
                int(BR[1]),
                int(BL[0]),
                int(BL[1]),
                1,
                jevois.YUYV.LightPink,
            )
            jevois.drawLine(
                outimg,
                int(BL[0]),
                int(BL[1]),
                int(TL[0]),
                int(TL[1]),
                1,
                jevois.YUYV.LightPink,
            )
            mx = int((TL[0] + TR[0]) / 2)
            my = int((TL[1] + TR[1]) / 2)

            self.drawPoint(outimg, [mx, my])

        except:
            jevois.sendSerial("Unable to draw detections.")

    # Draws a dot for a given point
    def drawPoint(self, outimg, point):
        try:
            jevois.drawLine(
                outimg,
                int(point[0]),
                int(point[1]),
                int(point[0]),
                int(point[1]),
                1,
                jevois.YUYV.LightPink,
            )
        except:
            jevois.sendSerial("Unable to draw point.")

    # ###################################################################################################
    ## Process function with no USB output
    def processNoUSB(self, inframe):
        # Get the next camera image (may block until it is captured) as OpenCV BGR:
        imgbgr = inframe.getCvBGR()
        h, w, chans = imgbgr.shape

        # Start measuring image processing time:
        self.timer.start()

        # Get a list of quadrilateral convex hulls for all good objects:
        found, distance, angle, skew = self.detect(imgbgr)

        # Load camera calibration if needed:
        if not hasattr(self, "camMatrix"):
            self.loadCameraCalibration(w, h)

        # Send all serial messages:
        self.sendAllSerial(imageAge, found, distance, angle, skew)

        # Log frames/s info (will go to serlog serial port, default is None):
        self.timer.stop()

    # ###################################################################################################
    ## Process function with USB output
    def process(self, inframe, outframe):
        # Get the next camera image (may block until it is captured). To avoid wasting much time assembling a composite
        # output image with multiple panels by concatenating numpy arrays, in this module we use raw YUYV images and
        # fast paste and draw operations provided by JeVois on those images:
        inimg = inframe.get()

        # Start measuring image processing time:
        self.timer.start()
        startTime = time.time()

        # Convert input image to BGR24:
        imgbgr = jevois.convertToCvBGR(inimg)

        # Let camera know we are done using the input image:
        inframe.done()

        # Load camera calibration if needed:

        h, w, chans = imgbgr.shape
        if not hasattr(self, "camMatrix"):
            self.loadCameraCalibration(w, h)

        # center of imgbgr
        center = (w / 2, h / 2)

        # imgbgr = cv2.undistort(imgbgr, self.camMatrix, self.distCoeffs, dst=None, newCameraMatrix = None)

        # Get pre-allocated but blank output image which we will send over USB:

        outimg = outframe.get()

        outimg.require("output", w * 2, h + 12, jevois.V4L2_PIX_FMT_YUYV)
        M = cv2.getRotationMatrix2D(center, self.img_rotate_angle, self.scale)
        imgbgr_rotate = cv2.warpAffine(imgbgr, M, (w, h))
        jevois.convertCvBGRtoRawImage(imgbgr_rotate, inimg, 0)
        jevois.paste(inimg, outimg, 0, 0)
        jevois.drawFilledRect(
            outimg, 0, h, outimg.width, outimg.height - h, jevois.YUYV.Black
        )

        imgbgr = cv2.warpAffine(imgbgr, M, (w, h))
        imgbgr = self.thresholding(imgbgr)

        # Get a list of quadrilateral convex hulls for all good objects:
        found, distance, angle, skew = self.detect(imgbgr, outimg)

        # calculate age of image (time since it was originally taken)
        now = time.time()
        imageAge = now - startTime

        # Send all serial messages:
        self.sendAllSerial(imageAge, found, distance, angle, skew)

        # Write frames/s info from our timer into the edge map (NOTE: does not account for output conversion time):
        fps = self.timer.stop()
        jevois.writeText(
            outimg, fps, 3, h - 10, jevois.YUYV.White, jevois.Font.Font6x10
        )

        # We are done with the output, ready to send it to host over USB:
        outframe.send()
