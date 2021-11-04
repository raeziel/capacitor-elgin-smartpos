import Foundation

@objc public class SmartPOS: NSObject {
    @objc public func echo(_ value: String) -> String {
        print(value)
        return value
    }
}
