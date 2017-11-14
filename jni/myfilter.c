/*
 * File: myfilter.c
 *
 * MATLAB Coder version            : 2.6
 * C/C++ source code generated on  : 01-Nov-2017 13:51:32
 */

/* Include files */
#include "rt_nonfinite.h"
#include "myfilter.h"
#include "myfilter_emxutil.h"
#include "filter.h"

/* Function Definitions */

/*
 * UNTITLED 此处显示有关此函数的摘要
 *    此处显示详细说明
 * Arguments    : const emxArray_real_T *source
 *                emxArray_real_T *result
 * Return Type  : void
 */
void myfilter(const emxArray_real_T *source, emxArray_real_T *result)
{
  int md2;
  int i0;
  emxArray_real_T *y;
  double xtmp;
  double d0;
  int loop_ub;
  double a[2];
  static const double b_a[2] = { 0.99445728278972167, -0.795259929455429 };

  emxArray_real_T *b_y;
  int m;
  emxArray_real_T *c_y;
  md2 = source->size[1];
  if (md2 == 0) {
    i0 = result->size[0] * result->size[1];
    result->size[0] = 0;
    result->size[1] = 0;
    emxEnsureCapacity((emxArray__common *)result, i0, (int)sizeof(double));
  } else {
    emxInit_real_T(&y, 1);
    xtmp = 2.0 * source->data[0];
    md2 = source->size[1];
    d0 = 2.0 * source->data[md2 - 1];
    md2 = source->size[1];
    i0 = y->size[0];
    y->size[0] = 12 + source->size[1];
    emxEnsureCapacity((emxArray__common *)y, i0, (int)sizeof(double));
    for (i0 = 0; i0 < 6; i0++) {
      y->data[i0] = xtmp - source->data[6 - i0];
    }

    loop_ub = source->size[1];
    for (i0 = 0; i0 < loop_ub; i0++) {
      y->data[i0 + 6] = source->data[i0];
    }

    for (i0 = 0; i0 < 6; i0++) {
      y->data[(i0 + source->size[1]) + 6] = d0 - source->data[(md2 - i0) - 2];
    }

    xtmp = y->data[0];
    for (i0 = 0; i0 < 2; i0++) {
      a[i0] = b_a[i0] * xtmp;
    }

    emxInit_real_T(&b_y, 1);
    i0 = b_y->size[0];
    b_y->size[0] = y->size[0];
    emxEnsureCapacity((emxArray__common *)b_y, i0, (int)sizeof(double));
    loop_ub = y->size[0];
    for (i0 = 0; i0 < loop_ub; i0++) {
      b_y->data[i0] = y->data[i0];
    }

    filter(b_y, a, y);
    m = y->size[0];
    i0 = y->size[0];
    md2 = i0 / 2;
    loop_ub = 1;
    emxFree_real_T(&b_y);
    while (loop_ub <= md2) {
      xtmp = y->data[loop_ub - 1];
      y->data[loop_ub - 1] = y->data[m - loop_ub];
      y->data[m - loop_ub] = xtmp;
      loop_ub++;
    }

    xtmp = y->data[0];
    for (i0 = 0; i0 < 2; i0++) {
      a[i0] = b_a[i0] * xtmp;
    }

    emxInit_real_T(&c_y, 1);
    i0 = c_y->size[0];
    c_y->size[0] = y->size[0];
    emxEnsureCapacity((emxArray__common *)c_y, i0, (int)sizeof(double));
    loop_ub = y->size[0];
    for (i0 = 0; i0 < loop_ub; i0++) {
      c_y->data[i0] = y->data[i0];
    }

    filter(c_y, a, y);
    m = y->size[0];
    i0 = y->size[0];
    md2 = i0 / 2;
    loop_ub = 1;
    emxFree_real_T(&c_y);
    while (loop_ub <= md2) {
      xtmp = y->data[loop_ub - 1];
      y->data[loop_ub - 1] = y->data[m - loop_ub];
      y->data[m - loop_ub] = xtmp;
      loop_ub++;
    }

    md2 = source->size[1];
    loop_ub = (int)((double)md2 + 6.0) - 7;
    i0 = result->size[0] * result->size[1];
    result->size[0] = 1;
    result->size[1] = (int)((double)md2 + 6.0) - 6;
    emxEnsureCapacity((emxArray__common *)result, i0, (int)sizeof(double));
    for (i0 = 0; i0 <= loop_ub; i0++) {
      result->data[result->size[0] * i0] = y->data[6 + i0];
    }

    emxFree_real_T(&y);
  }

  /* 对滤波的数据求最大值 */
}

/*
 * File trailer for myfilter.c
 *
 * [EOF]
 */
