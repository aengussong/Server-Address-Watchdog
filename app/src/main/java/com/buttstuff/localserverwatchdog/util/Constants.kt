package com.buttstuff.localserverwatchdog.util

// todo these consts should be private in the File Logger class. Even better, they should be retrieved from the strings.xml
//  the fact that these are public is bad, 'cause this string is separately used to log successful check in the Logger class
//  and separate check in other class uses these constants to identify whether last checkup was OK. So if Logger will use
//  some third constant - wasLastCheckupSuccessful function won't crash, but will start working incorrectly. And as there
//  is no good tests coverage and no CI set, the consequences will be bad.
const val SERVER_STATUS_UP = "OK"
const val SERVER_STATUS_DOWN = "DOWN"

const val ERROR_POINTER = ">"
