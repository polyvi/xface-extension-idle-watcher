
/*
 Copyright 2012-2013, Polyvi Inc. (http://polyvi.github.io/openxface)
 This program is distributed under the terms of the GNU General Public License.

 This file is part of xFace.

 xFace is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 xFace is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with xFace.  If not, see <http://www.gnu.org/licenses/>.
 */

#import "XIdleWatcherExt.h"
#import <Cordova/CDVPluginResult.h>
#import <XFace/XConstants.h>
#import <XFace/XApplication.h>
#import <Cordova/NSArray+Comparisons.h>
#import <XFace/XUtils.h>

#define DEFAULT_TIMEOUT_INTERVAL    @(300)

//FIXME:有多个app时，每个app的超时时间不是独自的超时时间，而是最后调用该扩展的app的超时时间

@implementation XIdleWatcherExt

- (void)start:(CDVInvokedUrlCommand*)command
{
    NSNumber *timeout = [command.arguments objectAtIndex:0 withDefault:DEFAULT_TIMEOUT_INTERVAL];
    CDVPluginResult* result;
    if ([timeout intValue] <= 0) {
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [result setKeepCallback:NO];
    } else {
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [result setKeepCallback:@(YES)];
        [[NSNotificationCenter defaultCenter] postNotification:[NSNotification notificationWithName:UPDATE_TIMEOUT_INTERVAL_NOTIFICATION object:nil userInfo:@{@"timeout": timeout}]];
    }
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];


    [[NSNotificationCenter defaultCenter] addObserverForName:XUIAPPLICATION_TIMEOUT_NOTIFICATION
                                                      object:nil
                                                       queue:[NSOperationQueue mainQueue]
                                                  usingBlock:^(NSNotification *note)
    {
        CDVPluginResult *result = nil;
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                               messageAsString:@"timeout"];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }];
}

- (void)stop:(CDVInvokedUrlCommand*)command
{
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    [[NSNotificationCenter defaultCenter] postNotification:[NSNotification notificationWithName:UPDATE_TIMEOUT_INTERVAL_NOTIFICATION object:nil userInfo:@{@"timeout": @(0)}]];
}

@end
