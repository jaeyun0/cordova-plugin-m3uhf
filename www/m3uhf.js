var exec = require('cordova/exec');

exports.RFIDEnable = function (arg0, success, error) {
    exec(success, error, 'm3uhf', 'RFIDEnable', [arg0]);
};

exports.inventory = function (arg0, success, error) {
    exec(success, error, 'm3uhf', 'inventory', [arg0]);
};

exports.inventory = function (arg0, success, error) {
    exec(success, error, 'm3uhf', 'inventory', [arg0]);
};

exports.getPower = function (arg0, success, error) {
    exec(success, error, 'm3uhf', 'getPower', [arg0]);
};

exports.setPower = function (arg0, success, error) {
    exec(success, error, 'm3uhf', 'setPower', [arg0]);
};

exports.memoryRead = function (arg0, arg1, arg2, arg3, success, error) {
    exec(success, error, 'm3uhf', 'memoryRead', [arg0, arg1, arg2, arg3]);
};

exports.memoryWrite = function (arg0, arg1, arg2, arg3, arg4, success, error) {
    exec(success, error, 'm3uhf', 'memoryWrite', [arg0, arg1, arg2, arg3, arg4]);
};

exports.memoryLock = function (arg0, arg1, arg2, arg3, arg4, arg5, success, error) {
    exec(success, error, 'm3uhf', 'memoryLock', [arg0, arg1, arg2, arg3, arg4, arg5]);
};

exports.memoryKill = function (arg0, success, error) {
    exec(success, error, 'm3uhf', 'memoryKill', [arg0]);
};