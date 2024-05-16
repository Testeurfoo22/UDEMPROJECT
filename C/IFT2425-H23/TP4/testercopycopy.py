import matplotlib.pyplot as plt
import math as m
import numpy as np

R = 0.1
D = 0.3
C = 0.25

H = 0.01

magnet = [[0, 1], [(-1/m.sqrt(2)), (-1/2)], [(1/m.sqrt(2)), (-1/2)]]

def fx(x, dx, y):
    sum = 0

    for z in magnet:
        r = m.sqrt(pow((z[0]-x),2) + pow((z[1]-y),2) + pow(D,2))
        sum += ((z[0]-x) / pow(r,3))

    return (-1*R*dx + sum - (C))

def fy(y, dy, x):
    sum = 0

    for z in magnet:
        r = m.sqrt(pow((z[0]-x),2) + pow((z[1]-y),2) + pow(D,2))
        sum += ((z[1]-y) / pow(r,3))
  
    return (-1*R*dy + sum - (C))

def RungeKuttaX(x, dx, y):
    j1 = H * (fx(x, dx, y))
    k1 = H * (dx)
    j2 = H * (fx((x + 1/4*k1), (dx + 1/4*j1), y))
    k2 = H * (dx + 1/4*j1)
    j3 = H * (fx((x + 3/32*k1 + 9/32*k2), (dx + 3/32*j1 + 9/32*j2), y))
    k3 = H * (dx + 3/32*j1 + 9/32*j2)
    j4 = H * (fx((x + 1932/2197*k1 - 7200/2197*k2 + 7296/2197*k3), (dx + 1932/2197*j1 - 7200/2197*j2 + 7296/2197*j3), y))
    k4 = H * (dx + 1932/2197*j1 - 7200/2197*j2 + 7296/2197*j3)
    j5 = H * (fx((x + 439/216*k1 - 8*k2 + 3680/513*k3 - 845/4104*k4), (dx + 439/216*j1 - 8*j2 + 3680/513*j3 - 845/4104*j4), y))
    k5 = H * (dx + 439/216*j1 - 8*j2 + 3680/513*j3 - 845/4104*j4)
    j6 = H * (fx((x - 8/27*k1 + 2*k2 - 3544/2565*k3 + 1859/4104*k4 - 11/40*k5), (dx - 8/27*j1 + 2*j2 - 3544/2565*j3 + 1859/4104*j4 - 11/40*j5), y))
    k6 = H * (dx - 8/27*j1 + 2*j2 - 3544/2565*j3 + 1859/4104*j4 - 11/40*j5)
    return (x + 16/135*k1 + 6656/12825*k3 + 28561/56430*k4 - 9/50*k5 + 2/55*k6, dx + 16/135*j1 + 6656/12825*j3 + 28561/56430*j4 - 9/50*j5 + 2/55*j6)
    
def RungeKuttaY(y, dy, x):
    j1 = H * (fx(y, dy, x))
    k1 = H * (dy)
    j2 = H * (fx((y + 1/4*k1), (dy + 1/4*j1), x))
    k2 = H * (dy + 1/4*j1)
    j3 = H * (fx((y + 3/32*k1 + 9/32*k2), (dy + 3/32*j1 + 9/32*j2), x))
    k3 = H * (dy + 3/32*j1 + 9/32*j2)
    j4 = H * (fx((y + 1932/2197*k1 - 7200/2197*k2 + 7296/2197*k3), (dy + 1932/2197*j1 - 7200/2197*j2 + 7296/2197*j3), x))
    k4 = H * (dy + 1932/2197*j1 - 7200/2197*j2 + 7296/2197*j3)
    j5 = H * (fx((y + 439/216*k1 - 8*k2 + 3680/513*k3 - 845/4104*k4), (dy + 439/216*j1 - 8*j2 + 3680/513*j3 - 845/4104*j4), x))
    k5 = H * (dy + 439/216*j1 - 8*j2 + 3680/513*j3 - 845/4104*j4)
    j6 = H * (fx((y - 8/27*k1 + 2*k2 - 3544/2565*k3 + 1859/4104*k4 - 11/40*k5), (dy - 8/27*j1 + 2*j2 - 3544/2565*j3 + 1859/4104*j4 - 11/40*j5), x))
    k6 = H * (dy - 8/27*j1 + 2*j2 - 3544/2565*j3 + 1859/4104*j4 - 11/40*j5)
    return (y + 16/135*k1 + 6656/12825*k3 + 28561/56430*k4 - 9/50*k5 + 2/55*k6, dy + 16/135*j1 + 6656/12825*j3 + 28561/56430*j4 - 9/50*j5 + 2/55*j6)


# Conditions initiales
X = [0.2]
dX = [0]
Y = [-1.6]
dY = [0]

for i in range(200):
    xi, dxi = RungeKuttaX(X[i], dX[i], Y[i])
    yi, dyi = RungeKuttaY(Y[i], dY[i], X[i])
    X.append(xi)
    Y.append(yi)

# Tracé des solutions
yx = []
yy = []
for z in range(len(X)):
    yx.append(X[z][0])
    yy.append(Y[z][0])
plt.scatter(yx, yy, s = 1)
plt.xlim([-2, 2])
plt.ylim([-2, 2])
plt.show()