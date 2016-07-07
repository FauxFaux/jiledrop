var app = angular.module('jiledrop', []);

app.controller('UploadController', function UploadController($http, $element, $scope) {
    var $ctrl = this;

    $ctrl.session = null;
    $ctrl.resumableSupported = new Resumable().support;

    $ctrl.login = function() {
        $ctrl.loginError = '';
        $ctrl.loading = true;
        $http.post('api/auth', {
            username: $ctrl.username,
            password: $ctrl.password
        }).then(function(resp) {
            $ctrl.session = resp.data.target;
            if (!$ctrl.session) {
                $ctrl.loginError = 'Invalid username or password';
            }
            $ctrl.loading = false;
        }, function(err) {
            $ctrl.loginError = 'Unknown problem logging in: ' + err.status;
            $ctrl.loading = false;
        });
    };

    $ctrl.sendFile = function() {
        $ctrl.loading = true;

        var ourId = randomFileId();

        var r = new Resumable({
            target: 'api/upload/' + $ctrl.session + '/chunked',
            chunkSize: 1 * 1000 * 1000,
            maxChunkRetries: 20,
            chunkRetryInterval: 2000,
            query: {
                fileId: ourId
            }
        });

        var showError = function(error) {
            $ctrl.fileError = 'Unknown error uploading file: ' + error.status;
            $ctrl.file = null;
            $ctrl.loading = false;
        };

        r.on('fileSuccess', (file) => {
            var totalChunks = file.chunks.length;
            $http.post('api/upload/' + $ctrl.session + '/complete', file.fileName).then(
                function(resp) {
                    $ctrl.loading = false;
                }, showError
            );
        });

        r.on('fileError', (file, message) => {
            $ctrl.fileError = 'Problem uploading file: ' + message;
            $ctrl.loading = false;
            $ctrl.file = null;
        });

        r.on('progress', () => {
            $scope.$apply(() => {
                $ctrl.progress = Math.floor(r.progress() * 98);
            });
        });

        r.on('fileAdded', () => {
            r.upload();
        });

        r.addFile($element[0].querySelector('input[type=file]').files[0]);
    };
});

function randomFileId() {
    var alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    var ret = "";

    for (var i = 0; i < 10; i++) {
        ret += alphabet.charAt(Math.floor(alphabet.length * Math.random()));
    }

    return ret;
}
