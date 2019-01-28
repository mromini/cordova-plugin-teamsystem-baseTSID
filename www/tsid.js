/*global cordova, module*/

module.exports = {
    encrypt: function (email, password, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "TSID", "encrypt", [email,password]);
    },
    decrypt: function (encryptedString, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "TSID", "decrypt", [encryptedString]);
    }
};
