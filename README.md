## COMMON 库文档
    此库采用mvvm绑定,kotlin语言开发,集成了常用的网络请求,弹窗,刷新等库,并提供了大量的扩展方法进行调用,都是日常项目开发需要用到


#### 扩展方法
    扩展方法均以base开头,均为顶级方法,可直接使用
1. 弹窗
    - baseShowBottomSheetDialog(activity:AppCompatActivity,list: List<String>,title: String?message:String?,positiveText:String?,negativeText:String?,waitPositiveClick:Boolean,listItemClick: ((index: Int, txt: String) -> Unit)?)  
展示一个底部弹出对话框


~~~[api]
post:/justForTest/api/user/userLogin

*string:user_name=15612345678#用户手机号
*string:user_pwd#密码

<<<
success
{
    "ret": 0, # 返回状态码
    "data": {
        "user_id": 1 // 用户id
    },
}
<<<
error
{
    "success": 0, // 请求成功!
    "user_name_wrong": 1, // 密码不正确!
    "insert_wrong": 2, // 数据库写入用户信息失败!
    "query_wrong": 3, // 获取用户信息失败!
}
~~~