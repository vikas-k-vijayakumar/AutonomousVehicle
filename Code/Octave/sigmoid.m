function g = sigmoid(z)
% SIGMOID Compute sigmoid functoon
%   J = SIGMOID(z) computes the sigmoid of z.
%
% Developed By: Vikas K Vijayakumar (kvvikas@yahoo.co.in) as part of the AutonomousVehicle Project

g = 1.0 ./ (1.0 + exp(-z));
end
