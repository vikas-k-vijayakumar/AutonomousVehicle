%% Train AutonomousVehicle Neural Network Version 2
%  Instructions
%  ------------
% 
%  This file contains code that will help train the Neural Network developed for the 
%  Autonomous Vehicle Project. This code supports up to 3 hidden layers
%
%  Developed By: Vikas K Vijayakumar (kvvikas@yahoo.co.in) as part of the AutonomousVehicle Project

%% Initialization
clear ; 
close all; 
clc

%% Setup the Neural Network Layer sizes (Excluding Bias Unit)
input_layer_size  = 11264;  % 176x64 Pixel Width x Pixel Height of the Training Set
hidden_layer_size = [50;40;30];   % hidden units
num_labels = 3;          % 3 possible outputs. 0 for Forward, 2 for Right and 3 for Left. 1 for Reverse is currently ignored
Hidden_Layer_Count = size(hidden_layer_size,1);

%  Neural Network Training Optimization Parameters
%  Maximum Iterations for the Neural Network training
nnMaxIterations = 1000;
%  Cost and Gradient Regularization
lambda = 0.01;

%% =========== Part 1: Loading Data =============

% Load Training Data
printf("Loading Training Sets ...\n");

ReadData=dlmread('Data//NNTraining//Release-1//TrainingVersion-2//Training//AVC_TrainingData_2015-06-30_22-07-51.csv');
X = ReadData(:, 1:input_layer_size);
y = ReadData(:,input_layer_size+1);
m = size(X, 1);

printf("Program paused. Press enter to continue.\n");
pause;


%% ================ Part 2: Initializing Parameters ================
%  Randomly initiaize the weights of the Neural Network
printf("\nInitializing Neural Network Parameters ...\n");
if (Hidden_Layer_Count == 1)
	initial_Theta1 = randInitializeWeights(input_layer_size, hidden_layer_size(1));
	initial_Theta2 = randInitializeWeights(hidden_layer_size(1), num_labels);
elseif (Hidden_Layer_Count == 2)
	initial_Theta1 = randInitializeWeights(input_layer_size, hidden_layer_size(1));
	initial_Theta2 = randInitializeWeights(hidden_layer_size(1), hidden_layer_size(2));
	initial_Theta3 = randInitializeWeights(hidden_layer_size(2), num_labels);
else
	initial_Theta1 = randInitializeWeights(input_layer_size, hidden_layer_size(1));
	initial_Theta2 = randInitializeWeights(hidden_layer_size(1), hidden_layer_size(2));
	initial_Theta3 = randInitializeWeights(hidden_layer_size(2), hidden_layer_size(3));
	initial_Theta4 = randInitializeWeights(hidden_layer_size(3), num_labels);
end

% Unroll parameters
if (Hidden_Layer_Count == 1)
	initial_nn_params = [initial_Theta1(:) ; initial_Theta2(:)];
elseif (Hidden_Layer_Count == 2)
	initial_nn_params = [initial_Theta1(:) ; initial_Theta2(:); initial_Theta3(:)];
else
	initial_nn_params = [initial_Theta1(:) ; initial_Theta2(:); initial_Theta3(:); initial_Theta4(:)];
end


%% =============== Part 3: Checking Gradients ===============
%  check if the Gradient Calculations are correct. This piece of code needs to be run only
%  to verify if the Cost and Gradient computation algorithm is implemented correctly.
% Verified that its working correctly, hence commenting

% printf("\nChecking Backpropagation (w/ Regularization) ... \n");

% Check gradients by running checkNNGradients  with a lambda = 3
% checkNNGradientsV2(3);

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
costFunction = @(p) nnCostAndGradFunctionV2(p, ...
                                   input_layer_size, ...
                                   hidden_layer_size, ...
                                   num_labels, X, y, lambda);

% Now, costFunction is a function that takes in only one argument (the
% neural network parameters)
[nn_params, cost] = fmincg(costFunction, initial_nn_params, options);

% Obtain Thetas back from nn_params
Theta1 = reshape(nn_params(1:((hidden_layer_size(1)) * (input_layer_size + 1) )),hidden_layer_size(1),(input_layer_size + 1));

if (Hidden_Layer_Count == 1)
	StartRecord_2 = (1 + (hidden_layer_size(1) * (input_layer_size + 1) ));
	Theta2 = reshape(nn_params(StartRecord_2:end),num_labels,(hidden_layer_size(1) + 1));
else
	StartRecord_2 = (1 + (hidden_layer_size(1) * (input_layer_size + 1) ));
	EndRecord_2 = ((StartRecord_2 - 1) + ((hidden_layer_size(2)) * (hidden_layer_size(1) + 1) ));
	Theta2 = reshape(nn_params(StartRecord_2:EndRecord_2),hidden_layer_size(2),(hidden_layer_size(1) + 1));
end

if (Hidden_Layer_Count == 2)
	StartRecord_3 = (1 + EndRecord_2);
	Theta3 = reshape(nn_params(StartRecord_3:end),num_labels,(hidden_layer_size(2) + 1));	
elseif (Hidden_Layer_Count > 2)
	StartRecord_3 = (1 + EndRecord_2);
	EndRecord_3 = ((StartRecord_3 - 1) + ((hidden_layer_size(3)) * (hidden_layer_size(2) + 1) ));
	Theta3 = reshape(nn_params(StartRecord_3:EndRecord_3),hidden_layer_size(3),(hidden_layer_size(2) + 1));
end

if (Hidden_Layer_Count == 3)
	StartRecord_4 = (1 + EndRecord_3);
	Theta4 = reshape(nn_params(StartRecord_4:end),num_labels,(hidden_layer_size(3) + 1));
end

printf("Training Complete. Program paused. Press enter to continue.\n");
pause;


%% ================= Part 5: Calculate Training Accuracy =================
%  After training the neural network, use it to predict the labels of the 
%  training set. This can be used to compute the training set accuracy.

if (Hidden_Layer_Count == 1)
	predictedLabels = predict(Theta1, Theta2, X);
elseif(Hidden_Layer_Count == 2)
	predictedLabels = predictForTwoHidden(Theta1, Theta2, Theta3, X);
else
	predictedLabels = predictForThreeHidden(Theta1, Theta2, Theta3, Theta4, X);
end
trainingAccuracyPercentage = mean(double(predictedLabels == y)) * 100;
printf("\nTraining Set Accuracy: %f\n", trainingAccuracyPercentage);


%% ================= Part 5: Save weights =================
%  Save the weights / activations of the various nodes in the layers
%  of the neural network

if (Hidden_Layer_Count == 1)
	save -ascii NNWeights_Theta1.txt Theta1;
	save -ascii NNWeights_Theta2.txt Theta2;
elseif (Hidden_Layer_Count == 2)
	save -ascii NNWeights_Theta1.txt Theta1;
	save -ascii NNWeights_Theta2.txt Theta2;
	save -ascii NNWeights_Theta3.txt Theta3;
else
	save -ascii NNWeights_Theta1.txt Theta1;
	save -ascii NNWeights_Theta2.txt Theta2;
	save -ascii NNWeights_Theta3.txt Theta3;
	save -ascii NNWeights_Theta4.txt Theta4;
end
save -ascii PredictedLabels.txt predictedLabels;