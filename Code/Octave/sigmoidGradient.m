function g = sigmoidGradient(z)
% SIGMOIDGRADIENT returns the gradient of the sigmoid function
% evaluated at z
%   g = SIGMOIDGRADIENT(z) computes the gradient of the sigmoid function
%   evaluated at z. 
%
% Developed By: Vikas K Vijayakumar (kvvikas@yahoo.co.in) as part of the AutonomousVehicle Project

g = zeros(size(z));

g=(1 ./ (1 .+ e .^((-1)*z))) .* (1.-(1 ./ (1 .+ e .^((-1)*z))));

end
