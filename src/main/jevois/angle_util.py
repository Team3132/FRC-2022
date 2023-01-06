import math

# converts degrees to radians
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
