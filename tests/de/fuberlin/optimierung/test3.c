#include <stdio.h>
#include <stdlib.h>

struct test {
  int a[5];
  float b;
};

int main(int argc, char **argv) {
  struct test a;
  a.a[3] = 5;
  a.b = 5.0;
  printf("int:%i",a.a[3]);
  return a.a[3];
} 
