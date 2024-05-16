import matplotlib.pyplot as plt
import math as m
import numpy as np

R = 0.1
D = 0.3
C = 0.25

H = 0.0001

magnet = [[0, 1], [(-1/m.sqrt(2)), (-1/2)], [(1/m.sqrt(2)), (-1/2)]]

def fx(arrayx, arrayy):
    sumX = 0
    x, vitX = arrayx
    y, vitY = arrayy

    for z in magnet:
        r = m.sqrt(pow((z[0]-x),2) + pow((z[1]-y),2) + pow(D,2))
        sumX += ((z[0]-x) / pow(r,3))

    return [vitX, (-1*R*vitX + sumX - (C)*x)]

def fy(arrayx, arrayy):
    sumY = 0
    x, vitX = arrayx
    y , vitY = arrayy

    for z in magnet:
        r = m.sqrt(pow((z[0]-x),2) + pow((z[1]-y),2) + pow(D,2))
        sumY += ((z[1]-y) / pow(r,3))
  
    return [vitY, (-1*R*vitY + sumY - (C)*y)]

def adder(array1, array2):
    return [array1[0] + array2[0], array1[1] + array2[1]]

def multi(array, int):
    return [array[0] * (int), array[1] * (int)]

def RungeKutta(arrayx, arrayy):
    k1x = multi(fx(arrayx, arrayy), H)
    k2x = multi(fx(adder(arrayx, multi(k1x, 1/4)), arrayy), H)
    k3x = multi(fx(adder(adder(arrayx, multi(k1x, 3/32)), multi(k2x, 9/32)), arrayy), H)
    k4x = multi(fx(adder(adder(adder(arrayx, multi(k1x, 1932/2197)), multi(k2x, -7200/2197)), multi(k3x, 7296/2197)), arrayy), H)
    k5x = multi(fx(adder(adder(adder(adder(arrayx, multi(k1x, 439/216)), multi(k2x, -8)), multi(k3x, 3680/513)), multi(k4x, -845/4104)), arrayy), H)
    k6y = multi(fx(adder(adder(adder(adder(adder(arrayx, multi(k1x, -8/27)), multi(k2x, 2)), multi(k3x, -3544/2565)), multi(k4x, 1859/4104)), multi(k5x, -11/40)), arrayy), H)
    k1y = multi(fy(arrayx, arrayy), H)
    k2y = multi(fy(arrayx, adder(arrayy, multi(k1y, 1/4))), H)
    k3y = multi(fy(arrayx, adder(adder(arrayy, multi(k1y, 3/32)), multi(k2y, 9/32))), H)
    k4y = multi(fy(arrayx, adder(adder(adder(arrayy, multi(k1y, 1932/2197)), multi(k2y, -7200/2197)), multi(k3y, 7296/2197))), H)
    k5y = multi(fy(arrayx, adder(adder(adder(adder(arrayy, multi(k1y, 439/216)), multi(k2y, -8)), multi(k3y, 3680/513)), multi(k4y, -845/4104))), H)
    k6x = multi(fy(arrayx, adder(adder(adder(adder(adder(arrayy, multi(k1y, -8/27)), multi(k2y, 2)), multi(k3y, -3544/2565)), multi(k4y, 1859/4104)), multi(k5y, -11/40))), H)
    return (adder(arrayx, adder(adder(adder(adder(multi(k1x, 16/135), multi(k3x, 6656/12825)), multi(k4x, 28561/56430)), multi(k5x, -9/50)), multi(k6x, 2/55)))
          , adder(arrayy, adder(adder(adder(adder(multi(k1y, 16/135), multi(k3y, 6656/12825)), multi(k4y, 28561/56430)), multi(k5y, -9/50)), multi(k6y, 2/55))))


# Conditions initiales
x = [[0.2, 0]]
y = [[-1.6, 0]]

for i in range(300000):
    xi, yi = RungeKutta(x[i], y[i])
    x.append(xi)
    y.append(yi)

# Tracé des solutions
yx = []
yy = []
for z in range(len(x)):
    yx.append(x[z][0])
    yy.append(y[z][0])
plt.scatter(yx, yy, s = 1)
plt.xlim([-2, 2])
plt.ylim([-2, 2])
plt.show()