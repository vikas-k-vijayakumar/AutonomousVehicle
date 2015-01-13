function W = randInitializeWeights(L_in, L_out)
% RANDINITIALIZEWEIGHTS Randomly initialize the weights of a layer with L_in
% incoming connections and L_out outgoing connections
%   W = RANDINITIALIZEWEIGHTS(L_in, L_out) randomly initializes the weights 
%   of a layer with L_in incoming connections and L_out outgoing 
%   connections. This is required so that the symmetry is broekn while
%   training the neural network
%
% Developed By: Vikas K Vijayakumar (kvvikas@yahoo.co.in) as part of the AutonomousVehicle Project


% Initialize Matrix. W should be set to a matrix of size(L_out, 1 + L_in) as
%   the column row of W handles the "bias" terms
W = zeros(L_out, 1 + L_in);

% Epsilon is used to denote the range over which the randoms must be generated. 
% Keeping it small by default
Epsilon=0.12;
W=(rand(L_out, 1+L_in) .* 2 .* Epsilon) .- Epsilon;

end
