/**
 * 文件上传控制器
 * author:  gomyck
 * qq: 474798383
 * email: hao474798383@163.com
 */
class CkFastDFS {

    constructor(option) {

        try{CkFastDFS.prototype.chunkMap.get(option.uploadButton.buttonId).destroy();}catch (e) {}

        CkFastDFS.prototype.SERVER_ERROR = "SERVER_ERROR";  //服务器错误

        CkFastDFS.prototype.FILE_IS_UPLOADED = "FILE_IS_UPLOADED"; //文件已上传过(秒传)

        CkFastDFS.prototype.Q_EXCEED_NUM_LIMIT  = "Q_EXCEED_NUM_LIMIT"; //上传数量上限
        CkFastDFS.prototype.Q_EXCEED_SIZE_LIMIT = "Q_EXCEED_SIZE_LIMIT"; //上传总量上限
        CkFastDFS.prototype.Q_TYPE_DENIED       = "Q_TYPE_DENIED"; //上传类型错误
        CkFastDFS.prototype.F_EXCEED_SIZE       = "F_EXCEED_SIZE"; //单个文件上限
        CkFastDFS.prototype.F_DUPLICATE         = "F_DUPLICATE"; //重复添加


        /**
         * 服务地址+服务上下文
         */
        this.baseURI = option.baseURI || "/";
        if (this.baseURI.lastIndexOf("/") == (this.baseURI.length - 1)) {
            this.baseURI = this.baseURI.substring(0, this.baseURI.length - 1);
        }
        this.checkURI      = this.baseURI + '/upload/chunkUpload/checkFile';
        this.uploadURI     = this.baseURI + '/upload/chunkUpload/uploadFile';
        this.configURI     = this.baseURI + '/upload/chunkUpload/config';
        this.maxFileSize   = 1024 * 1024 * 200; //200MB
        this.chunkSize     = 1024 * 1024 * 5; //5MB
        this.fileServerUrl = "http://127.0.0.1/";//文件服务器的IP, 如果需要预览文件或下载文件, 可能需要这个IP
        this.fastDFSGroup  = option.fastDFSGroup || "";

        /**
         * WebUpLoader配置 参照 webUpLoaderConfig
         */
    this.uploaderConfig = option.uploaderConfig || {};
        /**
         * 上传按钮
         *
         * 按钮选择器: buttonId[Selector]
         * 是否开启多文件选择: multiple[boolean]
         */
        this.uploadButton = option.uploadButton;
        /**
         * 进度条
         *
         * 改变进度条: changeBar(file, progressVal[百分比进度])
         */
        this.uploadProgressBar = null;
        /**
         * 上传监听 参照 initUploaderListener()
         */
        this.uploadListener = null;
        this.uploaderStatus = $.Deferred();
        this.initProgressBar(option.uploadProgressBar);//初始化进度条配置
        this.initUploaderListener(option.uploadListener);//初始化监听配置
        if (!CkFastDFS.prototype.hasOwnProperty("initOnce")) {
            CkFastDFS.prototype.chunkMap = this.initMap(); //存储分块信息的map
            //初始化标记, webUploader事件只注册一次, 否则出现重复调用
            this.registerWebUploader(); //注册webUpLoader默认项
            CkFastDFS.prototype.initOnce = true;
        }
        this.uploader   = null; //上传插件实例
        this.uploaderId = this.initWebUpLoader();//上传插件ID
        this.initUpLoaderEvent();//注册事件
    }

    /**
     * 初始化监听配置
     * @param config
     */
    initUploaderListener(config) {
        const defaultUploadListener = {
            //添加文件信息
            appendFileInfo: function (refer, file) {
                //console.log("选择文件: " + refer + "|||" + file.id);
            },
            //添加到上传队列之前
            beforeAppendFileInQueued: function (refer, file) {
                //console.log("添加到队列之前: " + refer + "|||" + file.id);
                return true;
            },
            //开始上传
            beginUpload: function (refer, file) {
                //console.log("开始上传: " + refer + "|||" + file.id);
            },
            //分块上传成功
            chunkUploadSuccess: function (refer, file, result) {
                //console.log("分块上传成功:" + refer + "|||" + file.id + "|||" + JSON.stringify(result))
            },
            //上传出错
            uploadError: function (refer, file, reason) {
                //console.log("上传失败: " + refer + "|||" + file.id + "|||" + reason);
            },
            //上传成功
            uploadSuccess: function (refer, file, result) {
                //console.log("上传成功: " + refer + "|||" + file.id + "|||" + JSON.stringify(result));
            },
            //上传完成
            uploadComplete: function (refer, file) {
                //console.log("上传完成: " + refer + "|||" + file.id);
            },
            //全局错误
            error: function (type, tips) {
                //console.log("全局错误: " + type + "|||" + tips)
            }
        };
        this.uploadListener         = Object.assign({}, defaultUploadListener, config)
    }

    /**
     * 初始化进度条配置
     * @param config
     */
    initProgressBar(config) {
        const defaultProgressBar = {
            changeBar: function (refer, file, progressVal) {
                console.log("进度条改变: " + refer + "|||" + file.id + "|||" + progressVal);
            }
        };
        this.uploadProgressBar   = Object.assign({}, defaultProgressBar, config)
    }

    /**
     * 注册一些事件(全局一次)
     */
    registerWebUploader() {
        WebUploader.Uploader.register({
                "before-send-file": "beforeSendFile",
                "before-send": "beforeSend"
            },
            {
                beforeSendFile: function (file) {
                    const fileId    = file.id;   //文件ID
                    const fileName  = file.name; //文件名称
                    const fileSize  = file.size; //文件大小
                    const task      = new $.Deferred();
                    let currentThis = this;
                    const _this     = currentThis.owner.ckInstance;
                    currentThis.owner.md5File(file).then(function (fileMd5) {
                        const url  = _this.checkURI;
                        const data = {
                            fileId: fileId,
                            fileName: fileName,
                            fileMd5: fileMd5,
                            fileSize: fileSize
                        };
                        _this.httpPostRequest(url, data, function (data) {
                            if (!data.isOk) {
                                _this.uploadListener.error(_this.SERVER_ERROR, data.resMsg);
                                task.reject();
                                return;
                            }
                            if (data.resCode == 302) {
                                _this.changeProgressBar(_this.getRefer(file), file, 1);
                                _this.uploadListener.uploadSuccess(_this.getRefer(file), file, data);
                                file.quickUp = true;
                                task.reject();
                                return;
                            }
                            if (!(data.resData.chunk >= 0)) {
                                _this.uploadListener.error(_this.SERVER_ERROR, '无法获取当前文件块, 请联系管理员');
                                task.reject();
                                return;
                            }
                            _this.chunkMap.put(currentThis.owner.ckId + fileId, {fileMd5: fileMd5, chunk: data.resData.chunk});
                            task.resolve();
                        }, function () {
                            _this.uploadListener.error(_this.SERVER_ERROR, '服务器错误, 请联系管理员');
                            task.reject();
                        })
                    });
                    return $.when(task);
                },
                beforeSend: function (block) {
                    const task   = new $.Deferred();
                    const fileId = this.owner.ckId + block.file.id;
                    const chunk  = this.owner.ckInstance.chunkMap.get(fileId).chunk; //当前第几块
                    if (block.chunk < chunk) {
                        task.reject();
                    } else {
                        console.debug("第" + block.chunk + "块开始上传");
                        task.resolve();
                    }
                    return $.when(task);
                }
            });
    }

    /**
     * 获取点击上传的元素jq对象
     * @param file
     * @returns {*}
     */
    getRefer(file) {
        return file.source._refer;
    }

    /**
     * 初始化webUploader
     * @returns {string}
     */
    initWebUpLoader() {
        const uploaderId      = "uploader_" + new Date().getTime() + "_" + parseInt(Math.random() * 1000, 10);
        const uploadURLString = this.uploadURI;
        let webUpLoaderConfig = {
            swf: this.baseURI + "/ck-fastdfs/swf/Uploader.swf",
            pick: { id: this.uploadButton.buttonId, multiple: this.uploadButton.multiple | this.uploadButton.directory | false, directory: this.uploadButton.directory | false }, //按钮的ID
            server: uploadURLString,
            accept: {
                title: '支持的文件类型',
                extensions: 'txt,jpg,jpeg,png,gif,bmp,doc,docx,pdf,xls,xlsx,ppt,pptx,zip,rar',
                mimeTypes: 'text/plain,/image/jpg,image/jpeg,image/png,image/gif,image/bmp,application/msword,' +
                    'application/vnd.openxmlformats-officedocument.wordprocessingml.document,application/pdf,' +
                    'application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,' +
                    'application/vnd.ms-powerpoint,application/vnd.openxmlformats-officedocument.presentationml.presentation,application/x-zip-compressed,application/x-rar-compressed'
            },
            runtimeOrder: "html5,flash",
            resize: false,
            compress: false,
            auto: true,
            chunkSize: this.chunkSize,
            chunked: true,
            fileSingleSizeLimit: this.maxFileSize,
            threads: 1,
            data: {},
            headers: {}
            // auto: true
        };
        const finalConfig     = Object.assign({}, webUpLoaderConfig, this.uploaderConfig);
        const _this           = this;
        this.loadServerConfig(function () {
            finalConfig.fileSingleSizeLimit = _this.maxFileSize; //从服务器读取的单个文件上传限制
            finalConfig.chunkSize           = _this.chunkSize;   //从服务器读取的单个文件上传限制
            _this.uploader                  = WebUploader.create(finalConfig);
            _this.uploader.ckId             = uploaderId;
            _this.uploader.ckInstance       = _this;
            CkFastDFS.prototype.chunkMap.put(_this.uploadButton.buttonId,  _this.uploader);
            _this.uploaderStatus.resolve();
        });
        return uploaderId;
    }

    /**
     * 初始化上传事件
     */
    initUpLoaderEvent() {
        const _this = this;
        $.when(this.uploaderStatus).done(function () {
            const _uploader = _this.uploader;
            _uploader.on('fileQueued', function (file) {
                _this.uploadListener.appendFileInfo(_this.getRefer(file), file);
            });
            _uploader.on('beforeFileQueued', function (file) {
                return _this.uploadListener.beforeAppendFileInQueued(_this.getRefer(file), file);
            });
            //此处是发送给服务端的参数
            _uploader.on('uploadBeforeSend', function (block, data, headers) {
                Object.assign(data, _this.uploaderConfig.data);
                Object.assign(headers, _this.uploaderConfig.headers);
                const fileId   = _this.uploader.ckId + block.file.id;
                data.fileMd5   = _this.chunkMap.get(fileId).fileMd5;
                data.chunkSize = block.blob.size;
                data.group     = _this.fastDFSGroup;
            });
            _uploader.on('uploadStart', function (file) {
                _this.uploadListener.beginUpload(_this.getRefer(file), file);
            });
            _uploader.on('uploadProgress', function (file, percentage) {
                _this.changeProgressBar(_this.getRefer(file), file, percentage);
            });
            _uploader.on('uploadAccept', function (obj, response) {
                try {
                    const result = JSON.parse(response._raw);
                    if (result.isOk) {
                        _this.uploadListener.chunkUploadSuccess(_this.getRefer(obj.file), obj.file, result);
                        return true;
                    }else{
                        _this.uploadListener.error(_this.SERVER_ERROR, result.resMsg)
                        return false
                    }
                } catch (err) {
                    _this.uploadListener.error(_this.SERVER_ERROR, "服务器返回信息错误, 请联系管理员");
                    console.error(err);
                    return false
                }
            });
            _uploader.on('uploadError', function (file, reason) {
                if (!file.quickUp) _this.uploadListener.uploadError(_this.getRefer(file), file, reason);
            });
            _uploader.on('uploadSuccess', function (file, response) {
                _this.changeProgressBar(_this.getRefer(file), file, 1);
                const result = JSON.parse(response._raw);
                if(!result.isOk){
                    _this.uploadListener.error(_this.SERVER_ERROR, result.resMsg)
                    return;
                }
                _this.uploadListener.uploadSuccess(_this.getRefer(file), file, result);

            });
            _uploader.on('uploadComplete', function (file) {
                _this.uploadListener.uploadComplete(_this.getRefer(file), file);
            });
            _uploader.on('error', function (type) {
                let tips = "";
                switch (type) {
                    case _this.Q_EXCEED_NUM_LIMIT:
                        tips = "文件数量已达上限";
                        break;
                    case _this.Q_EXCEED_SIZE_LIMIT:
                        tips = "上传文件总大小超过上限";
                        break;
                    case _this.Q_TYPE_DENIED:
                        tips = "不支持的文件类型";
                        break;
                    case _this.F_EXCEED_SIZE:
                        tips = "单个文件大小已超上限";
                        break;
                    case _this.F_DUPLICATE:
                        tips = "文件队列里已存在";
                        break;
                }
                _this.uploadListener.error(type, tips);
            });
        });
    }

    /**
     * 改变进度条包装方法
     * @param refer
     * @param file
     * @param percentage
     */
    changeProgressBar(refer, file, percentage) {
        const progressVal = Math.min(this.toDecimal(percentage * 100, 2), 100);
        this.uploadProgressBar.changeBar(refer, file, progressVal);
    }

    /**
     * post请求
     * @param url
     * @param data
     * @param success
     * @param error
     */
    httpPostRequest(url, data, success, error) {
        if(!!data) data = Object.assign(data, this.uploaderConfig.data);
        $.ajax({
            headers: this.uploaderConfig.headers,
            type: "POST",
            url: url,
            data: data,
            cache: false,
            dataType: "json",
            success: success,
            error: error
        });
    }

    /**
     * get请求
     * @param url
     * @param data
     * @param success
     * @param error
     */
    httpGetRequest(url, data, success, error) {
        if(!!data) data = Object.assign(data, this.uploaderConfig.data);
        $.ajax({
            headers: this.uploaderConfig.headers,
            type: "GET",
            url: url,
            data: data,
            cache: false,
            dataType: "json",
            success: success,
            error: error
        });
    }

    /**
     * map工具类
     */
    initMap() {
        const _this    = {};
        _this.elements = [];
        //获取MAP元素个数
        _this.size     = function () {
            return _this.elements.length;
        };
        //判断MAP是否为空
        _this.isEmpty  = function () {
            return (_this.elements.length < 1);
        };
        //删除MAP所有元素
        _this.clear    = function () {
            _this.elements = [];
        };
        //向MAP中增加元素（key, value)
        _this.put      = function (_key, _value) {
            const exist = _this.containsKey(_key);
            if (exist) {
                _this.remove(_key);
            }
            _this.elements.push({
                key: _key,
                value: _value
            });
        };
        //删除指定KEY的元素，成功返回True，失败返回False
        _this.remove   = function (_key) {
            let bln = false;
            try {
                for (let i = 0; i < _this.elements.length; i++) {
                    if (_this.elements[i].key == _key) {
                        _this.elements.splice(i, 1);
                        return true;
                    }
                }
            } catch (e) {
                bln = false;
            }
            return bln;
        };
        //获取指定KEY的元素值VALUE，失败返回NULL
        _this.get      = function (_key) {
            try {
                for (let i = 0; i < _this.elements.length; i++) {
                    if (_this.elements[i].key == _key) {
                        return _this.elements[i].value;
                    }
                }
            } catch (e) {
                return null;
            }
        };

        //获取指定索引的元素（使用element.key，element.value获取KEY和VALUE），失败返回NULL
        _this.element = function (_index) {
            if (_index < 0 || _index >= _this.elements.length) {
                return null;
            }
            return _this.elements[_index];
        };

        //判断MAP中是否含有指定KEY的元素
        _this.containsKey = function (_key) {
            let bln = false;
            try {
                for (let i = 0; i < _this.elements.length; i++) {
                    if (_this.elements[i].key == _key) {
                        bln = true;
                    }
                }
            } catch (e) {
                bln = false;
            }
            return bln;
        };

        //判断MAP中是否含有指定VALUE的元素
        _this.containsValue = function (_value) {
            let bln = false;
            try {
                for (let i = 0; i < _this.elements.length; i++) {
                    if (_this.elements[i].value == _value) {
                        bln = true;
                    }
                }
            } catch (e) {
                bln = false;
            }
            return bln;
        };

        //获取MAP中所有VALUE的数组（ARRAY）
        _this.values = function () {
            let arr = [];
            for (let i = 0; i < _this.elements.length; i++) {
                arr.push(_this.elements[i].value);
            }
            return arr;
        };

        //获取MAP中所有KEY的数组（ARRAY）
        _this.keys = function () {
            let arr = [];
            for (let i = 0; i < _this.elements.length; i++) {
                arr.push(_this.elements[i].key);
            }
            return arr;
        };

        return _this;
    }

    /**
     * 转化进度条百分比
     * @param x
     * @param n
     * @returns {number}
     */
    toDecimal(x, n) {
        n     = arguments[1] ? arguments[1] : 2;
        let f = parseFloat(x);

        function getN(n) {
            let sum = 1;
            for (let i = 0; i < n; i++) {
                sum *= 10;
            }
            return sum;
        }

        if (isNaN(f)) {
            return 0;
        }
        f = Math.round(x * getN(n)) / getN(n);
        return f;
    }

    /**
     * 获取服务器配置
     * @param callBack
     */
    loadServerConfig(callBack) {
        this.httpGetRequest(this.configURI, null, function (data) {
            this.maxFileSize   = data.resData.maxFileSize;
            this.chunkSize     = data.resData.chunkSize;
            this.fileServerUrl = data.resData.fileServerUrl;
            callBack();
        }, function () {
            console.error('获取配置失败!');
        });
    };


    //-----一下为webUploader包装方法, 如需更多, 请参考webUploader api

    /**
     * 为当前上传实例新增一个按钮
     * @param selector
     */
    addButton(selector) {
        const _this = this;
        $.when(this.uploaderStatus).done(function () {
            if (typeof selector == "string") selector = [selector];
            for (const index in selector) {
                _this.uploader.addButton({
                    id: selector[index]
                });
            }
        });
    }

    /**
     * 暂停上传
     * @param param 布尔值时, 为暂停正在上传的文件, file类型时, 暂停指定file的上传, null|undefined时为暂停
     */
    pauseUpload(param) {
        if (param) {
            this.uploader.stop(param);
        } else {
            this.uploader.stop();
        }
    }

    /**
     * 取消上传
     * @param file 文件信息
     */
    cancleUpload(file) {
        if (file) {
            this.uploader.cancelFile(file);
        } else {
            console.error("no file info can be cancel")
        }
    }

}