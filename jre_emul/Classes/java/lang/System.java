/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.lang;

/*-[
#import "IOSBooleanArray.h"
#import "IOSByteArray.h"
#import "IOSCharArray.h"
#import "IOSDoubleArray.h"
#import "IOSFloatArray.h"
#import "IOSIntArray.h"
#import "IOSLongArray.h"
#import "IOSObjectArray.h"
#import "IOSShortArray.h"
#import "java/lang/ArrayIndexOutOfBoundsException.h"
#import "java/lang/ArrayStoreException.h"
#import "java/lang/IllegalArgumentException.h"
#import "java/lang/NullPointerException.h"
#include "mach/mach_time.h"
]-*/

import java.io.BufferedInputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple iOS version of java.lang.System.  No code was shared, just its
 * public API.
 * 
 * @author Tom Ball
 */
public class System {
  private static Properties props;

  public static final InputStream in;
  public static final PrintStream out;
  public static final PrintStream err;

  /*-[
    static mach_timebase_info_data_t machTimeInfo_;
  ]-*/
  
  static {
    // Set up standard in, out, and err.
    err = new PrintStream(new FileOutputStream(FileDescriptor.err));
    out = new PrintStream(new FileOutputStream(FileDescriptor.out));
    in = new BufferedInputStream(new FileInputStream(FileDescriptor.in));

    // Set up statics for time unit conversion.
    setTimeInfoConsts();
  }

  private static native void setTimeInfoConsts() /*-[
    // Get the timebase info
    mach_timebase_info(&machTimeInfo_);
  ]-*/;

  public static native void setIn(InputStream newIn) /*-[
#if __has_feature(objc_arc)
    JavaLangSystem_in_ = newIn;
#else
    JreOperatorRetainedAssign(&JavaLangSystem_in_, self, newIn);
#endif
  ]-*/;

  public static native void setOut(java.io.PrintStream newOut) /*-[
#if __has_feature(objc_arc)
    JavaLangSystem_out_ = newOut;
#else
    JreOperatorRetainedAssign(&JavaLangSystem_out_, self, newOut);
#endif
  ]-*/;

  public static native void setErr(java.io.PrintStream newErr)  /*-[
#if __has_feature(objc_arc)
    JavaLangSystem_err_ = newErr;
#else
    JreOperatorRetainedAssign(&JavaLangSystem_err_, self, newErr);
#endif
  ]-*/;

  public static native long currentTimeMillis() /*-[
    return (long long) ([[NSDate date] timeIntervalSince1970] * 1000);
  ]-*/;

  public static native int identityHashCode(Object anObject) /*-[
    return (int) (intptr_t) anObject;
  ]-*/;
  
  public static native void arraycopy(Object src, int srcPos, Object dest, int destPos,
      int length) /*-[
    if (!src || !dest) {
      @throw AUTORELEASE([[JavaLangNullPointerException alloc] init]);
    }
    if (![src isKindOfClass:[IOSArray class]]) {
      NSString *msg = [NSString stringWithFormat:@"source of type %@ is not an array",
                       [src class]];
      @throw AUTORELEASE([[JavaLangArrayStoreException alloc] initWithNSString:msg]);
    }
    if (![dest isKindOfClass:[IOSArray class]]) {
      NSString *msg = [NSString stringWithFormat:@"destination of type %@ is not an array",
                       [dest class]];
      @throw AUTORELEASE([[JavaLangArrayStoreException alloc] initWithNSString:msg]);
    }
    if (![dest isMemberOfClass:[src class]]) {
      NSString *msg =
         [NSString stringWithFormat:@"source type %@ cannot be copied to array of type %@",
          [src class], [dest class]];
      @throw AUTORELEASE([[JavaLangArrayStoreException alloc] initWithNSString:msg]);
    }
    
    // Check for negative positions and length, since the array classes use unsigned ints.
    if (srcPos < 0 || destPos < 0 || length < 0) {
      @throw AUTORELEASE([[JavaLangArrayIndexOutOfBoundsException alloc] init]);
    }

    // Range tests are done by array class.
    [(IOSArray *) src arraycopy:NSMakeRange(srcPos, length)
                    destination:(IOSArray *) dest
                         offset:destPos];
  ]-*/;

  public native static long nanoTime() /*-[
    uint64_t time = mach_absolute_time();

    // Convert to nanoseconds and return,
    return (time * machTimeInfo_.numer) / machTimeInfo_.denom;
  ]-*/;

  public native static void exit(int status) /*-[
    exit(status);
  ]-*/;

  public static Properties getProperties() {
    if (props == null) {
      props = new Properties();
      props.setProperty("os.name", "Mac OS X");
      props.setProperty("file.separator", "/");
      props.setProperty("line.separator", "\n");
      props.setProperty("path.separator", ":");
      props.setProperty("org.xml.sax.driver", "org.xmlpull.v1.sax2.Driver");
      setSystemProperties(props);
    }
    return props;
  }

  private static native void setSystemProperties(Properties props) /*-[
    [props setPropertyWithNSString:@"user.home" withNSString:NSHomeDirectory()];
    [props setPropertyWithNSString:@"user.name" withNSString:NSUserName()];
    NSString *curDir = [[NSFileManager defaultManager] currentDirectoryPath];
    [props setPropertyWithNSString:@"user.dir" withNSString:curDir];

    NSString *tmpDir = NSTemporaryDirectory();
    int iLast = [tmpDir length] - 1;
    if (iLast >= 0 && [tmpDir characterAtIndex:iLast] == '/') {
      tmpDir = [tmpDir substringToIndex:iLast];
    }
    [props setPropertyWithNSString:@"java.io.tmpdir" withNSString:tmpDir];
    [props setPropertyWithNSString:@"java.home" withNSString:[[NSBundle mainBundle] bundlePath]];
  ]-*/;

  public static String getProperty(String key) {
    return getProperties().getProperty(key);
  }

  public static String getProperty(String key, String defaultValue) {
    String result = getProperties().getProperty(key);
    return result != null ? result : defaultValue;
  }

  public static void setProperty(String key, String value) {
    getProperties().setProperty(key, value);
  }

  public static void setProperties(Properties properties) {
    props = properties;
  }

  public static String clearProperty(String key) {
    Properties properties = getProperties();
    String result = properties.getProperty(key);
    properties.remove(key);
    return result;
  }

  /**
   * Returns null. Android does not use {@code SecurityManager}. This method
   * is only provided for source compatibility.
   *
   * @return null
   */
  public static SecurityManager getSecurityManager() {
      return null;
  }

  /**
   * Returns the system's line separator.
   * @since 1.7
   */
  public static String lineSeparator() {
      return "\n";   // Always return OSX/iOS value.
  }

  // Android internal logging methods, rewritten to use Logger.

  /**
   * @hide internal use only
   */
  public static void logE(String message) {
      log(Level.SEVERE, message, null);
  }

  /**
   * @hide internal use only
   */
  public static void logE(String message, Throwable th) {
      log(Level.SEVERE, message, th);
  }

  /**
   * @hide internal use only
   */
  public static void logI(String message) {
      log(Level.INFO, message, null);
  }

  /**
   * @hide internal use only
   */
  public static void logI(String message, Throwable th) {
      log(Level.INFO, message, th);
  }

  /**
   * @hide internal use only
   */
  public static void logW(String message) {
      log(Level.WARNING, message, null);
  }

  /**
   * @hide internal use only
   */
  public static void logW(String message, Throwable th) {
      log(Level.WARNING, message, th);
  }

  private static Logger systemLogger;

  private static void log(Level level, String message, Throwable thrown) {
    if (systemLogger == null) {
      systemLogger = Logger.getLogger("java.lang.System");
    }
    systemLogger.log(level, message, thrown);
  }
}
