//
//  demo
//
//  Created by QL on 16/6/7.
//  Copyright © 2016年 demo. All rights reserved.
//

import UIKit

class JsonTool: NSObject {

}


public func toJSONString(dict: NSDictionary)->String{
    
    if let data : NSData = try? NSJSONSerialization.dataWithJSONObject(dict, options:[]) {
        var jsonStr = String(data: data, encoding: NSUTF8StringEncoding)
        jsonStr = jsonStr!.stringByTrimmingCharactersInSet(NSCharacterSet.whitespaceAndNewlineCharacterSet())
        return jsonStr!
        
    }
    return ""
}

public func toJSONObject(jsonStr: String) -> [String:AnyObject]? {
    
    if let json = jsonStr.stringByRemovingPercentEncoding{
        //首先判断能不能转换
        //        if (!NSJSONSerialization.isValidJSONObject(json)) {
        //            assert(false, "is not a valid json object")
        //            return nil
        //        }
        
        let data = json.dataUsingEncoding(NSUTF8StringEncoding)
        if let aData = data {
            if let dict = try? NSJSONSerialization.JSONObjectWithData(aData, options: NSJSONReadingOptions.MutableContainers){
                return dict as? [String : AnyObject]
            }
        }
    }
    
    return nil
}
