; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.

(ns ^:no-doc noahtheduke.splint.rules.helpers)

(defn deref?? [sexp]
  (case sexp
    (deref splint/deref) true
    false))

(defn syntax-quote?? [sexp]
  (= 'splint/syntax-quote sexp))

(defn unquote?? [sexp]
  (case sexp
    (unquote splint/unquote) true
    false))

(defn unquote-splicing?? [sexp]
  (case sexp
    (unquote-splicing splint/unquote-splicing) true
    false))

(defn var?? [sexp]
  (case sexp
    (var splint/var) true
    false))

(defn read-eval?? [sexp]
  (= 'splint/read-eval sexp))

(defn fn?? [sexp]
  (case sexp
    (fn fn* splint/fn) true
    false))

(defn re-pattern?? [sexp]
  (case sexp
    (re-pattern splint/re-pattern) true
    false))

(defn default-import? [sexp]
  (case sexp
    ;; bare
    (AbstractMethodError
      Appendable
      ArithmeticException
      ArrayIndexOutOfBoundsException
      ArrayStoreException
      AssertionError
      BigDecimal
      BigInteger
      Boolean
      Byte
      Callable
      CharSequence
      Character
      Class
      ClassCastException
      ClassCircularityError
      ClassFormatError
      ClassLoader
      ClassNotFoundException
      CloneNotSupportedException
      Cloneable
      Comparable
      Compiler
      Deprecated
      Double
      Enum
      EnumConstantNotPresentException
      Error
      Exception
      ExceptionInInitializerError
      Float
      IllegalAccessError
      IllegalAccessException
      IllegalArgumentException
      IllegalMonitorStateException
      IllegalStateException
      IllegalThreadStateException
      IncompatibleClassChangeError
      IndexOutOfBoundsException
      InheritableThreadLocal
      InstantiationError
      InstantiationException
      Integer
      InternalError
      InterruptedException
      Iterable
      LinkageError
      Long
      Math
      NegativeArraySizeException
      NoClassDefFoundError
      NoSuchFieldError
      NoSuchFieldException
      NoSuchMethodError
      NoSuchMethodException
      NullPointerException
      Number
      NumberFormatException
      Object
      OutOfMemoryError
      Override
      Package
      Process
      ProcessBuilder
      Readable
      Runnable
      Runtime
      RuntimeException
      RuntimePermission
      SecurityException
      SecurityManager
      Short
      StackOverflowError
      StackTraceElement
      StrictMath
      String
      StringBuffer
      StringBuilder
      StringIndexOutOfBoundsException
      SuppressWarnings
      System
      Thread
      Thread$State
      Thread$UncaughtExceptionHandler
      ThreadDeath
      ThreadGroup
      ThreadLocal
      Throwable
      TypeNotPresentException
      UnknownError
      UnsatisfiedLinkError
      UnsupportedClassVersionError
      UnsupportedOperationException
      VerifyError
      VirtualMachineError
      Void) true
    ;; qualified
    (java.lang.AbstractMethodError
      java.lang.Appendable
      java.lang.ArithmeticException
      java.lang.ArrayIndexOutOfBoundsException
      java.lang.ArrayStoreException
      java.lang.AssertionError
      java.lang.Boolean
      java.lang.Byte
      java.lang.Callable
      java.lang.CharSequence
      java.lang.Character
      java.lang.Class
      java.lang.ClassCastException
      java.lang.ClassCircularityError
      java.lang.ClassFormatError
      java.lang.ClassLoader
      java.lang.ClassNotFoundException
      java.lang.CloneNotSupportedException
      java.lang.Cloneable
      java.lang.Comparable
      java.lang.Compiler
      java.lang.Deprecated
      java.lang.Double
      java.lang.Enum
      java.lang.EnumConstantNotPresentException
      java.lang.Error
      java.lang.Exception
      java.lang.ExceptionInInitializerError
      java.lang.Float
      java.lang.IllegalAccessError
      java.lang.IllegalAccessException
      java.lang.IllegalArgumentException
      java.lang.IllegalMonitorStateException
      java.lang.IllegalStateException
      java.lang.IllegalThreadStateException
      java.lang.IncompatibleClassChangeError
      java.lang.IndexOutOfBoundsException
      java.lang.InheritableThreadLocal
      java.lang.InstantiationError
      java.lang.InstantiationException
      java.lang.Integer
      java.lang.InternalError
      java.lang.InterruptedException
      java.lang.Iterable
      java.lang.LinkageError
      java.lang.Long
      java.lang.Math
      java.lang.NegativeArraySizeException
      java.lang.NoClassDefFoundError
      java.lang.NoSuchFieldError
      java.lang.NoSuchFieldException
      java.lang.NoSuchMethodError
      java.lang.NoSuchMethodException
      java.lang.NullPointerException
      java.lang.Number
      java.lang.NumberFormatException
      java.lang.Object
      java.lang.OutOfMemoryError
      java.lang.Override
      java.lang.Package
      java.lang.Process
      java.lang.ProcessBuilder
      java.lang.Readable
      java.lang.Runnable
      java.lang.Runtime
      java.lang.RuntimeException
      java.lang.RuntimePermission
      java.lang.SecurityException
      java.lang.SecurityManager
      java.lang.Short
      java.lang.StackOverflowError
      java.lang.StackTraceElement
      java.lang.StrictMath
      java.lang.String
      java.lang.StringBuffer
      java.lang.StringBuilder
      java.lang.StringIndexOutOfBoundsException
      java.lang.SuppressWarnings
      java.lang.System
      java.lang.Thread
      java.lang.Thread$State
      java.lang.Thread$UncaughtExceptionHandler
      java.lang.ThreadDeath
      java.lang.ThreadGroup
      java.lang.ThreadLocal
      java.lang.Throwable
      java.lang.TypeNotPresentException
      java.lang.UnknownError
      java.lang.UnsatisfiedLinkError
      java.lang.UnsupportedClassVersionError
      java.lang.UnsupportedOperationException
      java.lang.VerifyError
      java.lang.VirtualMachineError
      java.lang.Void
      java.math.BigDecimal
      java.math.BigInteger) true
    false))
