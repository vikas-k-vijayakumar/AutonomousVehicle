function [J grad] = nnCostAndGradFunction(nn_params, ...
                                   input_layer_size, ...
                                   hidden_layer_size, ...
                                   num_labels, ...
                                   X, y, lambda)
% NNCOSTANDGRADFUNCTION Implements the neural network cost function and gradients for a two layer
% neural network which performs classification
%   [J grad] = NNCOSTFUNCTON(nn_params, hidden_layer_size, num_labels, ...
%   X, y, lambda) computes the cost and gradient of the neural network. The
%   parameters for the neural network are "unrolled" into the vector
%   nn_params and need to be converted back into the weight matrices. 
% 
%   The returned parameter grad should be a "unrolled" vector of the
%   partial derivatives of the neural network.
%
%	Developed By: Vikas K Vijayakumar (kvvikas@yahoo.co.in) as part of the AutonomousVehicle Project


% Reshape nn_params back into the parameters Theta1 and Theta2, the weight matrices
% for the 2 layer neural network. Theta1 is the weight matrix for the input layer
% and Theta2 is the weight matrix for the hidden layer
Theta1 = reshape(nn_params(1:hidden_layer_size * (input_layer_size + 1)), ...
                 hidden_layer_size, (input_layer_size + 1));

Theta2 = reshape(nn_params((1 + (hidden_layer_size * (input_layer_size + 1))):end), ...
                 num_labels, (hidden_layer_size + 1));

% Some useful variables
m = size(X, 1);
         
% The following variables will be returned 
J = 0;
Theta1_grad = zeros(size(Theta1));
Theta2_grad = zeros(size(Theta2));

% Part 1: Feedforward the neural network and return the cost in the
%         variable J with regularization.

% Add the bias unit to the input layer
X=[ones(size(X,1),1) X];

% There are a total of 'num_labels' unique values possible for the Output Layer. Find out those unique values from y in the training data
Unique_OutNode_Values=unique(y);

% Initialize a temporary matrix which will have as many rows as number of training set
% and as many columns as number of unique output node values. Every enrty will have a binary value (0,1)
% Row entries in a certain column K represent if the y value of the correponding training set
% is equal to the k'th unique output value (1) or not (0). Hence every row can have only one 
% column with a value 0.
Binary_Y_Matrix=zeros(size(y,1),Unique_OutNode_Values);

%Assign valus to the binary matrix
for k=1:size(Unique_OutNode_Values,1)
	Binary_Y_Matrix(:,k)= y==Unique_OutNode_Values(k);
	k=k+1;
end

% Initialize and assign values to the matrix for Hidden layer Activations. Also add the bias unit
HiddenLayerActivations = [ones(size(X,1), 1) sigmoid(X * (Theta1)')];

% Calculate the cost for the output layer
for i=1:size(Unique_OutNode_Values,1)
	J = J + ((-1) * (((Binary_Y_Matrix(:,i))' * log(sigmoid(HiddenLayerActivations * (Theta2(i,:))'))) + ((1 .- (Binary_Y_Matrix(:,i)))' * log(1 .- sigmoid(HiddenLayerActivations * (Theta2(i,:))')))) / (m));
	i=i+1;
end

% Perform Cost Regularization
Theta1_WithoutBias=Theta1(:,2:end);
Theta2_WithoutBias=Theta2(:,2:end);

Theta1_Square=([Theta1_WithoutBias(:)] .^ 2)' * ones(size(Theta1_WithoutBias(:),1),1);
Theta2_Square=([Theta2_WithoutBias(:)] .^ 2)' * ones(size(Theta2_WithoutBias(:),1),1);

CostRegulization=(lambda / (2 * m)) * (Theta1_Square + Theta2_Square);
J=J+CostRegulization;


% Part 2: Use Backpropagation algorithm to compute the gradients
%         Theta1_grad and Theta2_grad. The partial derivatives of
%         the cost function with respect to Theta1 and Theta2 in Theta1_grad and
%         Theta2_grad respectively are returned. 

% Initialize the delta matrix for hidden and input layers
Delta_HiddenLayer=zeros(size(Theta2));
Delta_InputLayer=zeros(size(Theta1));

% Calculate the error and delta for every training set and accumulate it
for t=1:m
	% Calculate the Activations for Hidden and Output Layer based on the Input Layer values (X) & Theta1, Theta2 values
	Activation_InputLayer=(X(t,:))';
	Activation_HiddenLayer=[1;sigmoid(Theta1 * Activation_InputLayer)];
	Activation_OutputLayer=[sigmoid(Theta2 * Activation_HiddenLayer)];	
	Y_ForTrainingSet=(Binary_Y_Matrix(t,:))';
	
	% Calculate the error in determining the values for Output Layer by comparing the values computed
	% by using Theta value with the actual Output (y). Then back propogate to compute the error for
	% Hidden Layer
	Error_OutputLayer = (Activation_OutputLayer - Y_ForTrainingSet);
	Error_HiddenLayer = ((Theta2(:,2:end))' * Error_OutputLayer) .* (sigmoidGradient(Theta1 * Activation_InputLayer));
	
	% Accumulate the errors computed for every Training Set in the Hidden and Input Layer
	Delta_HiddenLayer = Delta_HiddenLayer .+ (Error_OutputLayer * (Activation_HiddenLayer)');
	Delta_InputLayer = Delta_InputLayer .+ (Error_HiddenLayer * (Activation_InputLayer)');

	t=t+1;
end

%Perform Gradient Regularization
Theta1_grad=(Delta_InputLayer + (lambda .* ([zeros(size(Theta1,1),1) Theta1(:,2:end)]))) ./ (m);
Theta2_grad=(Delta_HiddenLayer + (lambda .* ([zeros(size(Theta2,1),1) Theta2(:,2:end)]))) ./ (m);

% Unroll gradients from Matrix to Vector
grad = [Theta1_grad(:) ; Theta2_grad(:)];

fprintf('\nTraining Cost = ', J);

end
