/**
 * @file	ch_idsia_tools_amico_AmiCoJavaPy.cc
 * @brief	JavaCallsPython main file
 *
 * @author Sergey Karakovskiy, sergey at idsia.ch ; Nikolay Sohryakov, Nikolay.Sohryakov at gmail.com
 *
 * This is a customized Mario AI specific version of AmiCo.
 * It supports the JavaPy direction. In this set up Mario AI benchmark is used
 * as active entity.
 */

#include <Python.h>
#include <jni.h>
#include "ch_idsia_tools_amico_AmiCoJavaPy.h"
#include "arrayutils.h"
#include <iostream>

static const char * AMICO_WARNING = "[AmiCo Warning] : ";
static const char * AMICO_ERROR = "[AmiCo Error] : ";
static const char * AMICO_INFO = "[AmiCo Info] : ";
static const char * AMICO_EXCEPTION = "[AmiCo Exception] : ";

static int ERROR_PYTHON_IS_NOT_INITIALIZED = -1;
static int ERROR_CLASS_NOT_FOUND = -2;
static int ERROR_METHOD_NOT_FOUND = -3;
static int SUCCESS = 0;

PyObject* mainModule;
PyObject* agentClass;
PyObject* classInstance;

PyObject* getActionMethod;
PyObject* getNameMethod;
PyObject* giveIntermediateRewardMethod;
PyObject* integrateObservationMethod;
PyObject* resetMethod;
PyObject* setObservationDetailsMethod;

const char* agentName;
const char* defaultAgentName = "AmiCo Agent";

void Custom_PyError() 
{ 
	PyObject *pName, *pModule, *pDict, *pFunc; 
	PyObject *pArgs, *pValue; 
	char tb_string[1024]; 
	std::string err_str ; 
 
	PyObject *temp, *exc_typ, *exc_val, *exc_tb; 
	err_str = ""; 

	PyErr_Fetch(&exc_typ,&exc_val,&exc_tb); 
	PyErr_NormalizeException(&exc_typ,&exc_val,&exc_tb); 

	pName = PyString_FromString("traceback"); 
	pModule = PyImport_Import(pName); 
	Py_DECREF(pName); 

	temp = PyObject_Str(exc_typ); 
	if (temp != NULL) 
	{ 
		err_str += PyString_AsString(temp); 
		err_str += "\n"; 
	} 
	temp = PyObject_Str(exc_val); 
	if (temp != NULL)
	{ 
		err_str += PyString_AsString(temp); 
	} 
	Py_DECREF(temp); 
	err_str += "\n"; 

	if (exc_tb != NULL && pModule != NULL ) 
	{ 
		pDict = PyModule_GetDict(pModule); 
		pFunc = PyDict_GetItemString(pDict, "format_tb"); 
		if (pFunc && PyCallable_Check(pFunc)) 
		{ 
			pArgs = PyTuple_New(1); 
			pArgs = PyTuple_New(1); 
			PyTuple_SetItem(pArgs, 0, exc_tb); 
			pValue = PyObject_CallObject(pFunc, pArgs); 
			if (pValue != NULL) 
			{ 
				int len = PyList_Size(pValue); 
				if (len > 0) 
				{ 
					PyObject *t,*tt; 
					int i; 
					char *buffer; 
					for (i = 0; i < len; i++) 
					{ 
						tt = PyList_GetItem(pValue,i); 
						t = Py_BuildValue("(O)",tt); 
						if 
						(!PyArg_ParseTuple(t,"s",&buffer)){ 
							return; 
						} 

						strcpy(tb_string,buffer); 
						err_str += tb_string; 
						err_str += "\n"; 
					} 
				} 
			} 
			Py_DECREF(pValue); 
			Py_DECREF(pArgs); 
		} 
	} 
	Py_DECREF(pModule); 

	std::cerr << "Py_Error: " << err_str.c_str() << std::endl; 
	PyErr_Restore(exc_typ, exc_val, exc_tb); 
	PyErr_Print(); 
	return;  
}

JNIEXPORT jint JNICALL
Java_ch_idsia_tools_amico_AmiCoJavaPy_initModule(JNIEnv* env,
                                            jobject obj,
                                            jstring moduleNameJ,
                                            jstring classNameJ)
{
    std::cout << AMICO_INFO << "Initializing python environment" << std::endl;
    Py_Initialize();
    if (Py_IsInitialized())
        std::cout << AMICO_INFO << "Python environment initialized successfully" << std::endl;
    else
    {
        std::cerr << AMICO_EXCEPTION << "Python environment initialization failed!" << std::endl;
        return ERROR_PYTHON_IS_NOT_INITIALIZED;
        //throw (AMICO_EXCEPTION + "Python environment initialization failed!");
    }

	/*PyObject *sys_path, *path;
	sys_path = PySys_GetObject("path");
	if (sys_path == NULL || !PyList_Check(sys_path)) 
	{   
		std::cerr << AMICO_EXCEPTION << "Python system path failed!" << std::endl;
        return ERROR_PYTHON_IS_NOT_INITIALIZED;
	}
	path = PyString_FromString(".");
	PyList_Insert(sys_path, 0, path);
	Py_DECREF(path);*/
	
	char *path;
	path = Py_GetPath();
	printf("%sPython search path is: %s\n", AMICO_INFO, path);

    const char* moduleName = (env)->GetStringUTFChars(moduleNameJ, NULL);
    const char* className = (env)->GetStringUTFChars(classNameJ, NULL);
    mainModule = PyImport_ImportModule(className);

    if (mainModule != NULL)
        std::cout << AMICO_INFO << "Main module " << className << " has been loaded successfully" << std::endl;
    else
    {
        std::cerr << AMICO_ERROR << "Main module " << className << " had not been loaded successfuly. Details:" << std::endl;
        Custom_PyError();
		return ERROR_PYTHON_IS_NOT_INITIALIZED;
    }
		
    agentClass = PyObject_GetAttrString(mainModule, className);
    Py_DECREF(mainModule);

    if (agentClass != NULL)
        std::cout << AMICO_INFO << "Class " << className << " found successfully" << std::endl;
    else
    {
        std::cerr << AMICO_ERROR << "Class " << className<< " not found" << std::endl;
        Custom_PyError();
        return ERROR_CLASS_NOT_FOUND;
    }

    PyObject* args = Py_BuildValue("()");
    classInstance = PyEval_CallObject(agentClass, args);
    if (classInstance != NULL)
        std::cout << AMICO_INFO << "Class instance created successfully" << std::endl;
    else
    {
        std::cerr << AMICO_ERROR << "Class instance creation failed" << std::endl;
        Custom_PyError();
        return ERROR_CLASS_NOT_FOUND;
    }
    Py_DECREF(args);

    getNameMethod = PyObject_GetAttrString(classInstance, "getName");
    resetMethod = PyObject_GetAttrString(classInstance, "reset");
    giveIntermediateRewardMethod = PyObject_GetAttrString(classInstance, "giveIntermediateReward");
    integrateObservationMethod = PyObject_GetAttrString(classInstance, "integrateObservation");
    getActionMethod = PyObject_GetAttrString(classInstance, "getAction");
    setObservationDetailsMethod = PyObject_GetAttrString(classInstance, "setObservationDetails");

    if (getNameMethod == NULL)
    {
        std::cerr << AMICO_EXCEPTION << "Method \"getName\" not found" << std::endl;
        return ERROR_METHOD_NOT_FOUND;
    }

    if (resetMethod == NULL)
    {
        std::cerr << AMICO_EXCEPTION << "Method \"reset\" not found" << std::endl;
        return ERROR_METHOD_NOT_FOUND;
    }

    if (giveIntermediateRewardMethod == NULL)
    {
        std::cerr << AMICO_EXCEPTION << "Method \"giveIntermediateReward\" not found" << std::endl;
        return ERROR_METHOD_NOT_FOUND;
    }

    if (integrateObservationMethod == NULL)
    {
        std::cerr << AMICO_EXCEPTION << "Method \"integrateObservation\" not found" << std::endl;
        return ERROR_METHOD_NOT_FOUND;
    }

    if (getActionMethod == NULL)
    {
        std::cerr << AMICO_EXCEPTION << "Method \"getAction\" not found" << std::endl;
        return ERROR_METHOD_NOT_FOUND;
    }

    if (setObservationDetailsMethod == NULL)
    {
        std::cerr << AMICO_EXCEPTION << "Method \"setObservationDetails\" not found" << std::endl;
        return ERROR_METHOD_NOT_FOUND;
    }

    std::cout << AMICO_INFO << "All methods loaded successfully" << std::endl;

    Py_DECREF(agentClass);

    return SUCCESS;
}

JNIEXPORT void JNICALL
Java_ch_idsia_tools_amico_AmiCoJavaPy_integrateObservation(JNIEnv* env,
                                                      jobject obj,
                                                      jintArray squashedObservation,
                                                      jintArray squashedEnemies,
                                                      jfloatArray marioPos,
                                                      jfloatArray enemiesPos,
                                                      jintArray marioState)
{
    PyObject* sqObs = convertJavaArrayToPythonArray<jintArray, jint>(env, squashedObservation, 'I');
    PyObject* sqEn = convertJavaArrayToPythonArray<jintArray, jint>(env, squashedEnemies, 'I');
    PyObject* mPos = convertJavaArrayToPythonArray<jfloatArray, jfloat>(env, marioPos, 'F');
    PyObject* enPos = convertJavaArrayToPythonArray<jfloatArray, jfloat>(env, enemiesPos, 'F');
    PyObject* mState = convertJavaArrayToPythonArray<jintArray, jint>(env, marioState, 'I');

    PyObject* obs = PyTuple_New(5);
    PyTuple_SET_ITEM(obs, (Py_ssize_t)0, sqObs);
    PyTuple_SET_ITEM(obs, (Py_ssize_t)1, sqEn);
    PyTuple_SET_ITEM(obs, (Py_ssize_t)2, mPos);
    PyTuple_SET_ITEM(obs, (Py_ssize_t)3, enPos);
    PyTuple_SET_ITEM(obs, (Py_ssize_t)4, mState);

    if (obs == NULL)
    {
        std::cerr << AMICO_ERROR << "Can not create new Python tuple in \"integrateObservation\" method" << std::endl;
        //TODO: perform Py_Finalize() to destroy Python environment? is it necessary here?
        return;
    }

    PyObject* res = PyEval_CallObject(integrateObservationMethod, obs);
    Py_DECREF(obs);
}

JNIEXPORT jstring JNICALL
Java_ch_idsia_tools_amico_AmiCoJavaPy_getName(JNIEnv* env, jobject obj)
{
    PyObject* args = Py_BuildValue("()");
    PyObject* res = PyEval_CallObject(getNameMethod, args);

    Py_DECREF(args);
    if (res == NULL)
    {
        std::cerr << AMICO_ERROR << "Method getName has returd nothing. Using default agent name: AmiCo Agent" << std::endl;
        return env->NewStringUTF(defaultAgentName);
    }
    if (!PyString_Check(res))
    {
        std::cerr << AMICO_ERROR << "Object return by getName method is not a string. Using default agent name: AmiCo Agent" << std::endl;
        return env->NewStringUTF(defaultAgentName);
    }

    char* str = PyString_AsString(res);
    return env->NewStringUTF(str);
}

JNIEXPORT jintArray JNICALL
Java_ch_idsia_tools_amico_AmiCoJavaPy_getAction(JNIEnv* env, jobject obj)
{
    PyObject* args = Py_BuildValue("()");
    PyObject* res = PyEval_CallObject(getActionMethod, args);
    Py_DECREF(args);

    if (!PyTuple_Check(res))
    {
        std::cerr << AMICO_ERROR << "Object return by getAction method is not a tuple" << std::endl; std::cerr.flush();
        return NULL;
    }

    int size = 6;
    int* ar = new int[size];
    for (int i = 0; i < size; i++)
        ar[i] = PyInt_AsLong(PyTuple_GetItem(res, i));
    
    jintArray array = convertPythonArrayToJavaArray<int, jintArray, jint>(env, ar, 'I', (unsigned)PyTuple_Size(res));
    return array;
}

JNIEXPORT void JNICALL
Java_ch_idsia_tools_amico_AmiCoJavaPy_giveIntermediateReward(JNIEnv* env, jobject obj, jfloat intermediateReward)
{
    PyObject* arg = PyFloat_FromDouble(intermediateReward);
    PyObject* args = PyTuple_New(1);
    PyTuple_SET_ITEM(args, (Py_ssize_t)0, arg);

    PyEval_CallObject(giveIntermediateRewardMethod, args);
    Py_DECREF(args);
}

JNIEXPORT void JNICALL
Java_ch_idsia_tools_amico_AmiCoJavaPy_reset(JNIEnv* env, jobject obj)
{
    PyObject* args = Py_BuildValue("()");
    PyEval_CallObject(resetMethod, args);
    Py_DECREF(args);
}

JNIEXPORT void JNICALL
Java_ch_idsia_tools_amico_AmiCoJavaPy_setObservationDetails(JNIEnv* env,
                                                            jobject obj,
                                                            jint rfWidth,
                                                            jint rfHeight,
                                                            jint egoRow,
                                                            jint egoCol)
{
    PyObject* width = PyInt_FromLong(rfWidth);
    PyObject* height = PyInt_FromLong(rfHeight);
    PyObject* row = PyInt_FromLong(egoRow);
    PyObject* col = PyInt_FromLong(egoCol);

    PyObject* args = PyTuple_New(4);
    PyTuple_SET_ITEM(args, (Py_ssize_t)0, width);
    PyTuple_SET_ITEM(args, (Py_ssize_t)1, height);
    PyTuple_SET_ITEM(args, (Py_ssize_t)2, row);
    PyTuple_SET_ITEM(args, (Py_ssize_t)3, col);

    PyEval_CallObject(setObservationDetailsMethod, args);
    Py_DECREF(args);
}


JNIEXPORT void JNICALL
Java_ch_idsia_tools_amico_AmiCoJavaPy_finalizePythonEnvironment(JNIEnv* env,
                                                                jobject obj)
{
    std::cout << "herehereherehere" << std::endl;
    if (!Py_IsInitialized())
    {
        return;
    }
    Py_Finalize();
    std::cout << AMICO_INFO << "Python environment finalization successfull" << std::endl;
}