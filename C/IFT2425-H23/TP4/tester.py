import matplotlib.pyplot as plt
import math as m

R = 0.1
D = 0.3
C = 0.25

H = 0.1

def f(array):
    sumX = 0
    sumY = 0
    x, vitX, y , vitY = array
    
    magnet = [[0, 1], [-1/m.sqrt(2), -1/2], [1/m.sqrt(2), -1/2]]

    for z in magnet:
        r = m.sqrt(pow((z[0]-x),2) + pow((z[1]-y),2) + pow(D,2))
        sumX += ((z[0]-x) / pow(r,3))
        sumY += ((z[1]-y) / pow(r,3))

    return [vitX, (-1*R*vitX + sumX - C*x), vitY, (-1*R*vitY + sumY - C*y)]

def adder(array1, array2):
    return [array1[0] + array2[0], array1[1] + array2[1], array1[2] + array2[2], array1[3] + array2[3]]

def multi(array, int):
    return [array[0] * int, array[1] * int, array[2] * int, array[3] * int]

def RungeKutta(array):
    k1 = multi(f(array), H)
    k2 = multi(f(adder(array, multi(k1, 1/4))), H)
    k3 = multi(f(adder(adder(array, multi(k1, 3/32)), multi(k2, 9/32))), H)
    k4 = multi(f(adder(adder(adder(array, multi(k1, 1932/2197)), multi(k2, -7200/2197)), multi(k3, 7296/2197))), H)
    k5 = multi(f(adder(adder(adder(adder(array, multi(k1, 739/216)), multi(k2, -8)), multi(k3, 3680/513)), multi(k4, -845/4104))), H)
    k6 = multi(f(adder(adder(adder(adder(adder(array, multi(k1, -8/27)), multi(k2, 2)), multi(k3, 3544/2565)), multi(k4, 1859/4104)), multi(k5, -11/40))), H)
    return adder(array, adder(adder(adder(adder(multi(k1, 16/135), multi(k3, 6656/12825)), multi(k4, 28561/56430)), multi(k5, -9/50)), multi(k6, 2/55)))


# Conditions initiales

#y = [[-(4*(50/127.0)-2), 0, -(4*(50/127.0)-2), 0]]
y = [[-1/2, 0, -1/2, 0]]
pas = 0
pasCons = 0
for k in range(200):
    y.append(RungeKutta(y[k]))
    
#print(pas)
    
# Tracé des solutions
yx = []
yy = []
for z in y:
    yx.append(z[0])
    yy.append(z[2])
plt.scatter(yx, yy, s = 1)
plt.xlim([-2, 2])
plt.ylim([-2, 2])
plt.show()
        