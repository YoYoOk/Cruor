#include <jni.h>
#include <android/log.h>
#include <com_yj_cruor_testing_main_ScanDisplayActivity.h>
extern "C"
{
	#include <myfilter.h>
	#include <myfilter_emxAPI.h>
	#include <log_help.h>
}
#define TAG "######"


/*
 * 低通滤波器实现 调用c程序
 */
JNIEXPORT jdoubleArray JNICALL Java_com_yj_cruor_1testing_main_ScanDisplayActivity_process_1Data
  (JNIEnv *env, jobject obj, jdoubleArray source){
	jint length = env->GetArrayLength(source);//获取数组的长度
	jdouble *source_arr = env->GetDoubleArrayElements(source, 0);//获取数组指针
//	//新建长度len数组
	jdoubleArray result = env -> NewDoubleArray(length);
	jdouble *result_arr = env->GetDoubleArrayElements(result, 0);

	//低通滤波器实现--模仿matlab
	emxArray_real_T *temp = emxCreateWrapper_real_T(result_arr,1,length);
//	emxArray_real_T *temp = emxCreate_real_T(1, length);
	//
	myfilter(emxCreateWrapper_real_T(source_arr,1,length), temp);
	//于2017.11.01 20:19:10  终于调试成功

	env->SetDoubleArrayRegion(result, 0, length, result_arr);
	env->SetDoubleArrayRegion(source, 0, length, source_arr);
	env->ReleaseDoubleArrayElements(source, source_arr, 0);//释放资源  切记不可忘记
	env-> ReleaseDoubleArrayElements(result, result_arr, 0);
	LOGE("######%d",length);
	return result;
}
