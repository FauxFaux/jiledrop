var app = angular.module('jiledrop', []);

app.controller('UploadController', function UploadController($http) {
    var $ctrl = this;

    $ctrl.session = null;

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
});
