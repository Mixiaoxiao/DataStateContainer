DataStateContainer
===============

DataStateContainer是一个专为处理数据刷新/加载逻辑业务(如微博信息流)而生的ViewGroup。

Screenshots 
-----

![DataStateContainer](https://raw.github.com/Mixiaoxiao/DataStateContainer/master/DataStateContainer.gif) 

Sample APK
-----

[DataStateContainerSample.apk](https://raw.github.com/Mixiaoxiao/DataStateContainer/master/DataStateContainerSample.apk)

Features 特性
-----

* 现支持ListView与RecyclerView(LinearLayoutManager.VERTICAL和GridLayoutManager)
* 零侵入Adapter
* 下拉刷新：回调下拉百分比，可显示刷新成功/失败的状态
* 上拉加载：支持autoLoadWhenScrollToLastItem与hasMoreDataToLoad，支持“点击重新加载”
* Empty状态：可设置是否点击来retry
* 很强的扩展性：只需YourView implements IRefreshView、ILoadView、IEmptyView，参见SimpleXxxView


Usage 使用方法
-----

* 见Sample


简单说几句实现原理
-----

* 下拉刷新和v4包的SwipeRefreshLayout类似，当列表View不可下滑时劫持触摸事件，RefreshView的位置由setScroll(0, dy)控制，这样DataStateContainer内部的所有子View均可跟手移动(包括EmptyView)
* 上拉加载修改了列表View的paddingBottom【特别注意】，将paddingBottom设置为LoadView的高度，监听滑动来将LoadView“附着于”列表的lastChild末端

需要Header|Footer？
-----

* ListView本身支持
* RecyclerView写个多type的Adapter即可（或自行封装一个简单的带Header的WrapperAdapter）


TODO 待做
--------

* 还需支持RecyclerView的StaggeredGridLayoutManager，ScrollableViewWrapper中处理即可

Developed By
------------

* Mixiaoxiao - <xiaochyechye@gmail.com> or <mixiaoxiaogogo@163.com>
* Coding blogs is shit. I just code my codes.


License
-----------

    Copyright 2016 Mixiaoxiao

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
