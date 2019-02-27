module.service('UserAvatar', function(Auth) {
    this.url = function(user, realm) {
        return authUrl + '/realms/' + realm.realm + '/avatar-provider/avatar/' + user.id + "?access_token=" + Auth.authz.token + "&" + + new Date().getTime();
    }
});

module.controller('UserAvatarCtrl', function($scope, $http, Notifications, UserAvatar) {
    $scope.avatarUrl = UserAvatar.url($scope.user, $scope.realm);

    $scope.uploadAvatar = function(files) {
        var fd = new FormData();
        //Take the first selected file
        fd.append("image", files[0]);
    
        $http.post($scope.avatarUrl, fd, {
            headers: {'Content-Type': undefined },
            transformRequest: angular.identity
        }).then(function() {
            Notifications.success("Your changes have been saved to the user.");
            $scope.avatarUrl = UserAvatar.url($scope.user, $scope.realm);
        }, function(error) {
            console.error(error);
            Notifications.error("Could not save the avatar");
        });        
    }
});
