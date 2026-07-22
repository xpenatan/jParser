#import <UIKit/UIKit.h>

#include <stdio.h>

int jparser_ios_teavm_main(int argc, char** argv);

static NSString* const SharedLibStatusFileName = @"jparser-ios-status.txt";

static void SharedLibWriteStatus(NSString* status) {
    NSString* statusPath = [NSTemporaryDirectory()
        stringByAppendingPathComponent:SharedLibStatusFileName];
    NSError* error = nil;
    if(![status writeToFile:statusPath
                  atomically:YES
                    encoding:NSUTF8StringEncoding
                       error:&error]) {
        NSLog(@"Unable to write SharedLib emulator status: %@", error);
    }
}

@interface SharedLibAppDelegate : UIResponder <UIApplicationDelegate>
@property(strong, nonatomic) UIWindow* window;
@property(strong, nonatomic) UIViewController* viewController;
@property(strong, nonatomic) UILabel* statusLabel;
@end

@implementation SharedLibAppDelegate

- (BOOL)application:(UIApplication*)application
        didFinishLaunchingWithOptions:(NSDictionary*)launchOptions {
    self.window = [[UIWindow alloc] initWithFrame:[UIScreen mainScreen].bounds];
    self.viewController = [[UIViewController alloc] init];
    self.viewController.view.backgroundColor = [UIColor systemGrayColor];

    self.statusLabel = [[UILabel alloc] init];
    self.statusLabel.translatesAutoresizingMaskIntoConstraints = NO;
    self.statusLabel.numberOfLines = 0;
    self.statusLabel.textAlignment = NSTextAlignmentCenter;
    self.statusLabel.textColor = [UIColor whiteColor];
    self.statusLabel.font = [UIFont boldSystemFontOfSize:28.0];
    self.statusLabel.text = @"SharedLib TeaVM C\nRunning in iOS Simulator...";
    [self.viewController.view addSubview:self.statusLabel];
    [NSLayoutConstraint activateConstraints:@[
        [self.statusLabel.centerXAnchor constraintEqualToAnchor:self.viewController.view.centerXAnchor],
        [self.statusLabel.centerYAnchor constraintEqualToAnchor:self.viewController.view.centerYAnchor],
        [self.statusLabel.leadingAnchor constraintGreaterThanOrEqualToAnchor:self.viewController.view.leadingAnchor constant:24.0],
        [self.statusLabel.trailingAnchor constraintLessThanOrEqualToAnchor:self.viewController.view.trailingAnchor constant:-24.0]
    ]];

    self.window.rootViewController = self.viewController;
    [self.window makeKeyAndVisible];

    SharedLibWriteStatus(@"SharedLib iOS TeaVM C emulator started\n");
    dispatch_async(dispatch_get_global_queue(QOS_CLASS_USER_INITIATED, 0), ^{
        @autoreleasepool {
            char* argv[] = { "SharedLibIOSC", NULL };
            int result = jparser_ios_teavm_main(1, argv);
            NSString* status = result == 0
                ? @"SharedLib iOS TeaVM C emulator passed\n"
                : [NSString stringWithFormat:@"SharedLib iOS TeaVM C emulator failed with %d\n", result];
            SharedLibWriteStatus(status);
            dispatch_async(dispatch_get_main_queue(), ^{
                if(result == 0) {
                    self.viewController.view.backgroundColor = [UIColor systemGreenColor];
                    self.statusLabel.text = @"SharedLib TeaVM C\nPASS";
                }
                else {
                    self.viewController.view.backgroundColor = [UIColor systemRedColor];
                    self.statusLabel.text = [NSString stringWithFormat:@"SharedLib TeaVM C\nFAILED (%d)", result];
                }
                fprintf(stderr, "%s", status.UTF8String);
                fflush(stderr);
            });
        }
    });
    return YES;
}

@end

int main(int argc, char** argv) {
    @autoreleasepool {
        return UIApplicationMain(argc, argv, nil, NSStringFromClass([SharedLibAppDelegate class]));
    }
}
