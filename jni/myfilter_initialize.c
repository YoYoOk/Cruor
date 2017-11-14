/*
 * File: myfilter_initialize.c
 *
 * MATLAB Coder version            : 2.6
 * C/C++ source code generated on  : 01-Nov-2017 13:51:32
 */

/* Include files */
#include "rt_nonfinite.h"
#include "myfilter.h"
#include "myfilter_initialize.h"

/* Function Definitions */

/*
 * Arguments    : void
 * Return Type  : void
 */
void myfilter_initialize(void)
{
  rt_InitInfAndNaN(8U);
}

/*
 * File trailer for myfilter_initialize.c
 *
 * [EOF]
 */
