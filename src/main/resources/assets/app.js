var app = angular.module('jiledrop', []);

app.controller('UploadController', function UploadController($http) {
    var $ctrl = this;

    $ctrl.state = 'login';

    $ctrl.login = function() {
        $ctrl.loginError = '';
        $ctrl.loading = true;
        $http.post('api/auth', {
            username: $ctrl.username,
            password: $ctrl.password
        }).then(function(resp) {
            alert(resp);
            $ctrl.loading = false;
        }, function(err) {
            $ctrl.loginError = 'Unknown problem logging in: ' + err.status;
            $ctrl.loading = false;
        });
    };
});
