/*
 * File: myfilter_emxutil.h
 *
 * MATLAB Coder version            : 2.6
 * C/C++ source code generated on  : 01-Nov-2017 13:51:32
 */

#ifndef __MYFILTER_EMXUTIL_H__
#define __MYFILTER_EMXUTIL_H__

/* Include files */
#include <stddef.h>
#include <stdlib.h>
#include <string.h>
#include "rtwtypes.h"
#include "myfilter_types.h"

/* Function Declarations */
extern void b_emxInit_real_T(emxArray_real_T **pEmxArray, int numDimensions);
extern void emxEnsureCapacity(emxArray__common *emxArray, int oldNumel, int
  elementSize);
extern void emxFree_real_T(emxArray_real_T **pEmxArray);
extern void emxInit_real_T(emxArray_real_T **pEmxArray, int numDimensions);

#endif

/*
 * File trailer for myfilter_emxutil.h
 *
 * [EOF]
 */
