var app = angular.module('jiledrop', []);

app.controller('UploadController', function UploadController($scope, $http) {
    $scope.login = function() {
        $scope.loginError = '';
        $scope.loading = true;
        $http.post('auth', {
            username: $scope.username,
            password: $scope.password
        }).then(function(resp) {
            alert(resp);
            $scope.loading = false;
        }, function(err) {
            $scope.loginError = 'Unknown problem logging in: ' + err.status;
            $scope.loading = false;
        });
    };
});
