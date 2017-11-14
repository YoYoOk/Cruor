/*
 * File: filter.c
 *
 * MATLAB Coder version            : 2.6
 * C/C++ source code generated on  : 01-Nov-2017 13:51:32
 */

/* Include files */
#include "rt_nonfinite.h"
#include "myfilter.h"
#include "filter.h"
#include "myfilter_emxutil.h"

/* Function Definitions */

/*
 * Arguments    : const emxArray_real_T *x
 *                const double zi[2]
 *                emxArray_real_T *y
 * Return Type  : void
 */
void filter(const emxArray_real_T *x, const double zi[2], emxArray_real_T *y)
{
  unsigned int unnamed_idx_0;
  int k;
  double dbuffer[3];
  int j;
  static const double dv0[3] = { 0.0055427172102806843, 0.011085434420561369,
    0.0055427172102806843 };

  double b_dbuffer;
  static const double dv1[3] = { 1.0, -1.778631777824585, 0.80080264666570777 };

  unnamed_idx_0 = (unsigned int)x->size[0];
  k = y->size[0];
  y->size[0] = (int)unnamed_idx_0;
  emxEnsureCapacity((emxArray__common *)y, k, (int)sizeof(double));
  for (k = 0; k < 2; k++) {
    dbuffer[k + 1] = zi[k];
  }

  for (j = 0; j + 1 <= x->size[0]; j++) {
    for (k = 0; k < 2; k++) {
      dbuffer[k] = dbuffer[k + 1];
    }

    dbuffer[2] = 0.0;
    for (k = 0; k < 3; k++) {
      b_dbuffer = dbuffer[k] + x->data[j] * dv0[k];
      dbuffer[k] = b_dbuffer;
    }

    for (k = 0; k < 2; k++) {
      dbuffer[k + 1] -= dbuffer[0] * dv1[k + 1];
    }

    y->data[j] = dbuffer[0];
  }
}

/*
 * File trailer for filter.c
 *
 * [EOF]
 */
