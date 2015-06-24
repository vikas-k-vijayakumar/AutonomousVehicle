%% Train AutonomousVehicle Neural Network

%  Instructions
%  ------------
% 
%  This file contains code that will help train the Neural Network developed for the 
%  Autonomous Vehicle Project.
%
%  Developed By: Vikas K Vijayakumar (kvvikas@yahoo.co.in) as part of the AutonomousVehicle Project

%% Initialization
clear ; close all; clc

%% Setup the Neural Network Layer sizes (Excluding Bias Unit)
input_layer_size  = 11264;  % 176x104 Pixel Width x Pixel Height of the Training Set
hidden_layer_size = 80;   % 50 hidden units
num_labels = 3;          % 3 possible outputs. 0 for Forward, 2 for Right and 3 for Left. 1 for Reverse is currently ignored

%  Neural Network Training Optimization Parameters
%  Maximum Iterations for the Neural Network training
nnMaxIterations = 30;
%  Cost and Gradient Regularization
lambda = 0.01;

%% =========== Part 1: Loading Data =============

% Load Training Data
printf("Loading Training Sets ...\n");

ReadData=dlmread('Data//Training//AVC_TrainingData_2015-06-24_10-42-18_Resized-64-x-176.csv');
X = ReadData(:, 1:input_layer_size);
y = ReadData(:,input_layer_size+1);
m = size(X, 1);

printf("Program paused. Press enter to continue.\n");
pause;


%% ================ Part 2: Initializing Parameters ================
%  Randomly initiaize the weights of the Neural Network

printf("\nInitializing Neural Network Parameters ...\n");

initial_Theta1 = randInitializeWeights(input_layer_size, hidden_layer_size);
initial_Theta2 = randInitializeWeights(hidden_layer_size, num_labels);

% Unroll parameters
initial_nn_params = [initial_Theta1(:) ; initial_Theta2(:)];


%% =============== Part 3: Checking Gradients ===============
%  check if the Gradient Calculations are correct. This piece of code needs to be run only
%  to verify if the Cost and Gradient computation algorithm is implemented correctly.
% Verified that its working correctly, hence commenting

% printf("\nChecking Backpropagation (w/ Regularization) ... \n");

% Check gradients by running checkNNGradients  with a lambda = 3
% checkNNGradients(3);

printf("Program paused. Press enter to continue.\n");
pause;


%% =================== Part 4: Training Neural Network ===================
%  Train the neural network by using "fmincg", which
%  is a function which works similarly to "fminunc". This
%  advanced optimizer is able to train the cost functions efficiently as
%  long as they are provided with the gradient computations.
%
printf("\nTraining Neural Network... \n");

%  Set the Options
options = optimset('MaxIter', nnMaxIterations);

% Create "short hand" for the cost function to be minimized
costFunction = @(p) nnCostAndGradFunction(p, ...
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

printf("Training Complete. Program paused. Press enter to continue.\n");
pause;


%% ================= Part 5: Calculate Training Accuracy =================
%  After training the neural network, use it to predict the labels of the 
%  training set. This can be used to compute the training set accuracy.

predictedLabels = predict(Theta1, Theta2, X);
trainingAccuracyPercentage = mean(double(predictedLabels == y)) * 100;
printf("\nTraining Set Accuracy: %f\n", trainingAccuracyPercentage);


%% ================= Part 5: Save weights =================
%  Save the weights / activations of the various nodes in the layers
%  of the neural network

save -ascii NNWeights_Theta1.txt Theta1;
save -ascii NNWeights_Theta2.txt Theta2;


