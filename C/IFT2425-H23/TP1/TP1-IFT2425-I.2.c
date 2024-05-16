//------------------------------------------------------
// module  : Tp-IFT2425-I.2.c
// author  : 
// date    : 
// version : 1.0
// language: C
// note    :
//------------------------------------------------------
//

//------------------------------------------------
// FICHIERS INCLUS -------------------------------
//------------------------------------------------
#include <stdio.h>
#include <math.h>
#include <stdlib.h>
#include <string.h>
#include <iostream>
#include <new>
#include <time.h>

//----------------------------------------------------------
//----------------------------------------------------------
// PROGRAMME PRINCIPAL -------------------------------------
//----------------------------------------------------------
//----------------------------------------------------------

double tab[10] = { 0.11, 0.24, 0.27, 0.52, 1.13, 1.54, 1.71, 1.84, 1.92, 2.01};
double epsilon = 1e-5;
double tolF = 1e-6;
double tolC = 1e-6;

double c = 0.25;
double c2 = 0.25;

double f(double C){
    double PxLn = 0.0;
    double P = 0.0;
    double Ln = 0.0;
    double len = sizeof(tab)/sizeof(double);
    for (int x = 0; x < len; x++){
        PxLn += (pow(tab[x],C))*(log(tab[x]));
        P += pow(tab[x],C);
        Ln += log(tab[x]);
    }
    return ((PxLn)/(P)) - (1/(C)) - (1/len)*(Ln);
}

double g(double C){
    return (-f(C+2*epsilon)+8*f(C+epsilon)-8*f(C-epsilon)+f(C-2*epsilon))/(12*epsilon);
}

double alpha(double C){
    double P = 0.0;
    double len = sizeof(tab)/sizeof(double);
    for (int x = 0; x < len; x++){
        P = pow(tab[x],C);
    }

    return pow((P/len),(1/C));
}

int main(int argc,char** argv){
    while ((abs(c - c2) >= tolC or abs(f(c)) >= tolF) and g(c) != 0){
        c = c - (f(c)/g(c));
        c2 = c;
    }
    double a = alpha(c);
    printf("Valeur de c: %.15lf\n",c);
    printf("Valeur de alpha: %.15lf\n",a);
}