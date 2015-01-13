%% Train AutonomousVehicle Neural Network

%  Instructions
%  ------------
% 
%  This file contains code that will help train the Neural Network developed for the 
%  Autonomous Vehicle Project..ural network has one input, output and hidden layer each
%
%  Developed By: Vikas K Vijayakumar (kvvikas@yahoo.co.in) as part of the AutonomousVehicle Project

%% Initialization
clear ; close all; clc

%% Setup the Neural Network Layer sizes (Exluding Bias Unit)
input_layer_size  = 25344;  % 176x144 Pixel Width x Pixel Height of the Training Set
hidden_layer_size = 50;   % 50 hidden units
num_labels = 4;          % 4 possible outputs. 0 for Forward, 1 for Reverse, 2 for Right and 3 for Left

%  Neural Network Training Optimization Paramters
%  Maximum Iterations for the Neural Network training
nnMaxIterations = 100;
%  Cost and Gradient Regulrization
lambda = 0.1;

%% =========== Part 1: Loading Data =============

% Load Training Data
fprintf('Loading Training Sets ...\n')

load('ex4data1.mat');
m = size(X, 1);

fprintf('Program paused. Press enter to continue.\n');
pause;


%% ================ Part 2: Initializing Parameters ================
%  Randomly initiaize the weights of the Neural Network

fprintf('\nInitializing Neural Network Parameters ...\n')

initial_Theta1 = randInitializeWeights(input_layer_size, hidden_layer_size);
initial_Theta2 = randInitializeWeights(hidden_layer_size, num_labels);

% Unroll parameters
initial_nn_params = [initial_Theta1(:) ; initial_Theta2(:)];


%% =============== Part 3: Checking Gradients ===============
%  check if the Gradient Calculations are correct. This piece of code needs to be run only
%  to verify if the Cost and Gradient computation algorithm is implemented correctly.
%

fprintf('\nChecking Backpropagation (w/ Regularization) ... \n')

%  Check gradients by running checkNNGradients  with a lambda = 3
checkNNGradients(3);

fprintf('Program paused. Press enter to continue.\n');
pause;


%% =================== Part 4: Training Neural Network ===================
%  Train the neural network by using "fmincg", which
%  is a function which works similarly to "fminunc". This
%  advanced optimizer is able to train the cost functions efficiently as
%  long as they are provided with the gradient computations.
%
fprintf('\nTraining Neural Network... \n')

%  Set the Options
options = optimset('MaxIter', nnMaxIterations);

% Create "short hand" for the cost function to be minimized
costFunction = @(p) nnCostFunction(p, ...
                                   input_layer_size, ...
                                   hidden_layer_size, ...
                                   num_labels, X, y, lambda);

% Now, costFunction is a function that takes in only one argument (the
% neural network parameters)
[nn_params, cost] = fmincg(costFunction, initial_nn_params, options);

% Obtain Theta1 and Theta2 back from nn_params
Theta1 = reshape(nn_params(1:hidden_layer_size * (input_layer_size + 1)), ...
                 hidden_layer_size, (input_layer_size + 1));

Theta2 = reshape(nn_params((1 + (hidden_layer_size * (input_layer_size + 1))):end), ...
                 num_labels, (hidden_layer_size + 1));

fprintf('Training Complete. Program paused. Press enter to continue.\n');
pause;


%% ================= Part 5: Calculate Training Accuracy =================
%  After training the neural network, use it to predict the labels of the 
%  training set. This can be used to compute the training set accuracy.

predictedLabels = predict(Theta1, Theta2, X);
trainingAccuracyPercentage = mean(double(pred == y)) * 100;
fprintf('\nTraining Set Accuracy: %f\n', trainingAccuracyPercentage);


