function [J grad] = nnCostAndGradFunctionV2(nn_params, ...
                                   input_layer_size, ...
                                   hidden_layers_size, ...
                                   num_labels, ...
                                   X, y, lambda)
%nnCostAndGradFunctionV2 Implements the neural network cost function for a multi layer(Max 5 hidden) neural network
%neural network which performs classification
%   [J grad] = NNCOSTFUNCTON(nn_params, hidden_layers_countAndsize, num_labels, ...
%   X, y, lambda) computes the cost and gradient of the neural network. The
%   parameters for the neural network are "unrolled" into the vector
%   nn_params and need to be converted back into the weight matrices. 
%	hidden_layers_size is a vector of max length 5 which contains the size or number of nodes exclusing the bias node
%	in each hidden layer.
% 
%   The returned parameter grad should be a "unrolled" vector of the
%   partial derivatives of the neural network.
%

m=size(X,1);

%Determine the number of hidden layers
Hidden_Layer_Count = size(hidden_layers_size,1);

% Reshape nn_params back into the parameters Theta1, Theta2...etc
%Simultaneously calculate the Cost Regulirization as well
%Also initialize the required parameters for Gradient calculation as well

CostRegulization=0;
Theta1 = reshape(nn_params(1:((hidden_layers_size(1)) * (input_layer_size + 1) )),hidden_layers_size(1),(input_layer_size + 1));
Theta1_WithoutBias = Theta1(:,2:end);
Theta1_Square = ([Theta1_WithoutBias(:)] .^ 2)' * ones(size(Theta1_WithoutBias(:),1),1);
CostRegulization = CostRegulization + ((lambda / (2 * m)) * (Theta1_Square));
Theta1_grad = zeros(size(Theta1));
Delta1_grad = zeros(size(Theta1));

if (Hidden_Layer_Count == 1)
	StartRecord_2 = (1 + (hidden_layers_size(1) * (input_layer_size + 1) ));
	Theta2 = reshape(nn_params(StartRecord_2:end),num_labels,(hidden_layers_size(1) + 1));
	FinalHiddenLayerTheta = Theta2;
	Theta2_WithoutBias = Theta2(:,2:end);
	Theta2_Square = ([Theta2_WithoutBias(:)] .^ 2)' * ones(size(Theta2_WithoutBias(:),1),1);
	CostRegulization = CostRegulization + ((lambda / (2 * m)) * (Theta2_Square));
	Theta2_grad = zeros(size(Theta2));
	Delta2_grad = zeros(size(Theta2));
else
	StartRecord_2 = (1 + (hidden_layers_size(1) * (input_layer_size + 1) ));
	EndRecord_2 = ((StartRecord_2 - 1) + ((hidden_layers_size(2)) * (hidden_layers_size(1) + 1) ));
	Theta2 = reshape(nn_params(StartRecord_2:EndRecord_2),hidden_layers_size(2),(hidden_layers_size(1) + 1));
	Theta2_WithoutBias = Theta2(:,2:end);
	Theta2_Square = ([Theta2_WithoutBias(:)] .^ 2)' * ones(size(Theta2_WithoutBias(:),1),1);
	CostRegulization = CostRegulization + ((lambda / (2 * m)) * (Theta2_Square));
	Theta2_grad = zeros(size(Theta2));
	Delta2_grad = zeros(size(Theta2));
end

if (Hidden_Layer_Count == 2)
	StartRecord_3 = (1 + EndRecord_2);
	Theta3 = reshape(nn_params(StartRecord_3:end),num_labels,(hidden_layers_size(2) + 1));
	FinalHiddenLayerTheta = Theta3;
	Theta3_WithoutBias = Theta3(:,2:end);
	Theta3_Square = ([Theta3_WithoutBias(:)] .^ 2)' * ones(size(Theta3_WithoutBias(:),1),1);
	CostRegulization = CostRegulization + ((lambda / (2 * m)) * (Theta3_Square));	
	Theta3_grad = zeros(size(Theta3));
	Delta3_grad = zeros(size(Theta3));	
elseif (Hidden_Layer_Count > 2)
	StartRecord_3 = (1 + EndRecord_2);
	EndRecord_3 = ((StartRecord_3 - 1) + ((hidden_layers_size(3)) * (hidden_layers_size(2) + 1) ));
	Theta3 = reshape(nn_params(StartRecord_3:EndRecord_3),hidden_layers_size(3),(hidden_layers_size(2) + 1));
	Theta3_WithoutBias = Theta3(:,2:end);
	Theta3_Square = ([Theta3_WithoutBias(:)] .^ 2)' * ones(size(Theta3_WithoutBias(:),1),1);
	CostRegulization = CostRegulization + ((lambda / (2 * m)) * (Theta3_Square));	
	Theta3_grad = zeros(size(Theta3));
	Delta3_grad = zeros(size(Theta3));	
end

if (Hidden_Layer_Count == 3)
	StartRecord_4 = (1 + EndRecord_3);
	Theta4 = reshape(nn_params(StartRecord_4:end),num_labels,(hidden_layers_size(3) + 1));
	FinalHiddenLayerTheta = Theta4;
	Theta4_WithoutBias = Theta4(:,2:end);
	Theta4_Square = ([Theta4_WithoutBias(:)] .^ 2)' * ones(size(Theta4_WithoutBias(:),1),1);
	CostRegulization = CostRegulization + ((lambda / (2 * m)) * (Theta4_Square));
	Theta4_grad = zeros(size(Theta4));
	Delta4_grad = zeros(size(Theta4));	
elseif (Hidden_Layer_Count > 3)
	StartRecord_4 = (1 + EndRecord_3);
	EndRecord_4 = ((StartRecord_4 - 1) + ((hidden_layers_size(4)) * (hidden_layers_size(3) + 1) ));
	Theta4 = reshape(nn_params(StartRecord_4:EndRecord_4),hidden_layers_size(4),(hidden_layers_size(3) + 1));
	Theta4_WithoutBias = Theta4(:,2:end);
	Theta4_Square = ([Theta4_WithoutBias(:)] .^ 2)' * ones(size(Theta4_WithoutBias(:),1),1);
	CostRegulization = CostRegulization + ((lambda / (2 * m)) * (Theta4_Square));
	Theta4_grad = zeros(size(Theta4));
	Delta4_grad = zeros(size(Theta4));
end

if (Hidden_Layer_Count == 4)
	StartRecord_5 = (1 + EndRecord_4);
	Theta5 = reshape(nn_params(StartRecord_5:end),num_labels,(hidden_layers_size(4) + 1));
	FinalHiddenLayerTheta = Theta5;
	Theta5_WithoutBias = Theta5(:,2:end);
	Theta5_Square = ([Theta5_WithoutBias(:)] .^ 2)' * ones(size(Theta5_WithoutBias(:),1),1);
	CostRegulization = CostRegulization + ((lambda / (2 * m)) * (Theta5_Square));
	Theta5_grad = zeros(size(Theta5));
	Delta5_grad = zeros(size(Theta5));	
elseif (Hidden_Layer_Count > 4)
	StartRecord_5 = (1 + EndRecord_4);
	EndRecord_5 = ((StartRecord_5 - 1) + ((hidden_layers_size(5)) * (hidden_layers_size(4) + 1) ));
	Theta5 = reshape(nn_params(StartRecord_5:EndRecord_5),hidden_layers_size(5),(hidden_layers_size(4) + 1));
	Theta5_WithoutBias = Theta5(:,2:end);
	Theta5_Square = ([Theta5_WithoutBias(:)] .^ 2)' * ones(size(Theta5_WithoutBias(:),1),1);
	CostRegulization = CostRegulization + ((lambda / (2 * m)) * (Theta5_Square));
	Theta5_grad = zeros(size(Theta5));
	Delta5_grad = zeros(size(Theta5));	
end

if (Hidden_Layer_Count == 5)
	StartRecord_6 = (1 + EndRecord_5);
	Theta6 = reshape(nn_params(StartRecord_6:end),num_labels,(hidden_layers_size(5) + 1));
	FinalHiddenLayerTheta = Theta6;
	Theta6_WithoutBias = Theta6(:,2:end);
	Theta6_Square = ([Theta6_WithoutBias(:)] .^ 2)' * ones(size(Theta6_WithoutBias(:),1),1);
	CostRegulization = CostRegulization + ((lambda / (2 * m)) * (Theta6_Square));
	Theta6_grad = zeros(size(Theta6));
	Delta6_grad = zeros(size(Theta6));	
end


% Setup some useful variables
m = size(X, 1);         
J = 0;

%Add the bias unit to Input Layer
X=[ones(size(X,1),1) X];

%Get all the unique values for y. The number of such values will be equal to the number of output nodes/num_labels
Unique_OutNode_Values=unique(y);

%Construct a matrix which will have the binary results of y w.r.t every output label.
Binary_Y_Matrix=zeros(size(y,1),Unique_OutNode_Values);

%Create the binary matrix having y values. Each column corresponds to one output node. Each row corresponds to a training set
for k=1:size(Unique_OutNode_Values,1)
	Binary_Y_Matrix(:,k)= y==Unique_OutNode_Values(k);
	k=k+1;
end

%Calculate activations of all the hidden layers
HiddenLayerActivations_1 = [ones(size(X,1), 1) sigmoid(X * (Theta1)')];
FinalHiddenLayerActivation=HiddenLayerActivations_1;

if (Hidden_Layer_Count > 1)
	HiddenLayerActivations_2 = [ones(size(HiddenLayerActivations_1,1), 1) sigmoid(HiddenLayerActivations_1 * (Theta2)')];
	FinalHiddenLayerActivation=HiddenLayerActivations_2;
end

if (Hidden_Layer_Count > 2)
	HiddenLayerActivations_3 = [ones(size(HiddenLayerActivations_2,1), 1) sigmoid(HiddenLayerActivations_2 * (Theta3)')];
	FinalHiddenLayerActivation=HiddenLayerActivations_3;
end

if (Hidden_Layer_Count > 3)
	HiddenLayerActivations_4 = [ones(size(HiddenLayerActivations_3,1), 1) sigmoid(HiddenLayerActivations_3 * (Theta4)')];
	FinalHiddenLayerActivation=HiddenLayerActivations_4;
end

if (Hidden_Layer_Count > 4)
	HiddenLayerActivations_5 = [ones(size(HiddenLayerActivations_4,1), 1) sigmoid(HiddenLayerActivations_4 * (Theta5)')];
	FinalHiddenLayerActivation=HiddenLayerActivations_5;
end

%Calculate J i.e the Cost of the neural network
for i=1:size(Unique_OutNode_Values,1)
	J = J + ((-1) * (((Binary_Y_Matrix(:,i))' * log(sigmoid(FinalHiddenLayerActivation * (FinalHiddenLayerTheta(i,:))'))) + ((1 .- (Binary_Y_Matrix(:,i)))' * log(1 .- sigmoid(FinalHiddenLayerActivation * (FinalHiddenLayerTheta(i,:))')))) / (m));
	i=i+1;
end

%Regularize Cost
J=J+CostRegulization


%Gradient Calculation with Regularization

%For Every Training set, calculate the error and delta
for t=1:m

	Y_ForTrainingSet=(Binary_Y_Matrix(t,:))';
	Activation_InputLayer=(X(t,:))';
	
	%Calculate Activations of input, output and each hidden layer for the t-th training set
	if (Hidden_Layer_Count == 1)
		Activation_HiddenLayer_1=[1;sigmoid(Theta1 * Activation_InputLayer)];
		Activation_OutputLayer=[sigmoid(Theta2 * Activation_HiddenLayer_1)];
	else
		Activation_HiddenLayer_1=[1;sigmoid(Theta1 * Activation_InputLayer)];
	end

	if (Hidden_Layer_Count == 2)
		Activation_HiddenLayer_2=[1;sigmoid(Theta2 * Activation_HiddenLayer_1)];
		Activation_OutputLayer=[sigmoid(Theta3 * Activation_HiddenLayer_2)];		
	elseif (Hidden_Layer_Count > 2)
		Activation_HiddenLayer_2=[1;sigmoid(Theta2 * Activation_HiddenLayer_1)];
	end

	if (Hidden_Layer_Count == 3)
		Activation_HiddenLayer_3=[1;sigmoid(Theta3 * Activation_HiddenLayer_2)];
		Activation_OutputLayer=[sigmoid(Theta4 * Activation_HiddenLayer_3)];			
	elseif (Hidden_Layer_Count > 3)
		Activation_HiddenLayer_3=[1;sigmoid(Theta3 * Activation_HiddenLayer_2)];
	end

	if (Hidden_Layer_Count == 4)
		Activation_HiddenLayer_4=[1;sigmoid(Theta4 * Activation_HiddenLayer_3)];
		Activation_OutputLayer=[sigmoid(Theta5 * Activation_HiddenLayer_4)];					
	elseif (Hidden_Layer_Count > 4)
		Activation_HiddenLayer_4=[1;sigmoid(Theta4 * Activation_HiddenLayer_3)];		
	end

	if (Hidden_Layer_Count == 5)
		Activation_HiddenLayer_5=[1;sigmoid(Theta5 * Activation_HiddenLayer_4)];
		Activation_OutputLayer=[sigmoid(Theta6 * Activation_HiddenLayer_5)];		
	end
	
	%Calculate Error of each node in each hidden layer and the output layer and 
	%the delta for input layer and each hidden layer
	Error_OutputLayer = (Activation_OutputLayer - Y_ForTrainingSet);
	
	if (Hidden_Layer_Count == 5)
		Error_HiddenLayer_5 = ((Theta6(:,2:end))' * Error_OutputLayer) .* (sigmoidGradient(Theta5 * Activation_HiddenLayer_4));		
		Error_HiddenLayer_4 = ((Theta5(:,2:end))' * Error_HiddenLayer_5) .* (sigmoidGradient(Theta4 * Activation_HiddenLayer_3));
		Error_HiddenLayer_3 = ((Theta4(:,2:end))' * Error_HiddenLayer_4) .* (sigmoidGradient(Theta3 * Activation_HiddenLayer_2));
		Error_HiddenLayer_2 = ((Theta3(:,2:end))' * Error_HiddenLayer_3) .* (sigmoidGradient(Theta2 * Activation_HiddenLayer_1));
		Error_HiddenLayer_1 = ((Theta2(:,2:end))' * Error_HiddenLayer_2) .* (sigmoidGradient(Theta1 * Activation_InputLayer));
		
		Delta6_grad = Delta6_grad .+ (Error_OutputLayer * (Activation_HiddenLayer_5)');
		Delta5_grad = Delta5_grad .+ (Error_HiddenLayer_5 * (Activation_HiddenLayer_4)');
		Delta4_grad = Delta4_grad .+ (Error_HiddenLayer_4 * (Activation_HiddenLayer_3)');
		Delta3_grad = Delta3_grad .+ (Error_HiddenLayer_3 * (Activation_HiddenLayer_2)');
		Delta2_grad = Delta2_grad .+ (Error_HiddenLayer_2 * (Activation_HiddenLayer_1)');
		Delta1_grad = Delta1_grad .+ (Error_HiddenLayer_1 * (Activation_InputLayer)');	
	end
	
	if (Hidden_Layer_Count == 4)
		Error_HiddenLayer_4 = ((Theta5(:,2:end))' * Error_OutputLayer) .* (sigmoidGradient(Theta4 * Activation_HiddenLayer_3));
		Error_HiddenLayer_3 = ((Theta4(:,2:end))' * Error_HiddenLayer_4) .* (sigmoidGradient(Theta3 * Activation_HiddenLayer_2));
		Error_HiddenLayer_2 = ((Theta3(:,2:end))' * Error_HiddenLayer_3) .* (sigmoidGradient(Theta2 * Activation_HiddenLayer_1));
		Error_HiddenLayer_1 = ((Theta2(:,2:end))' * Error_HiddenLayer_2) .* (sigmoidGradient(Theta1 * Activation_InputLayer));	

		Delta5_grad = Delta5_grad .+ (Error_OutputLayer * (Activation_HiddenLayer_4)');
		Delta4_grad = Delta4_grad .+ (Error_HiddenLayer_4 * (Activation_HiddenLayer_3)');
		Delta3_grad = Delta3_grad .+ (Error_HiddenLayer_3 * (Activation_HiddenLayer_2)');
		Delta2_grad = Delta2_grad .+ (Error_HiddenLayer_2 * (Activation_HiddenLayer_1)');
		Delta1_grad = Delta1_grad .+ (Error_HiddenLayer_1 * (Activation_InputLayer)');		
	end
	
	if (Hidden_Layer_Count == 3)
		Error_HiddenLayer_3 = ((Theta4(:,2:end))' * Error_OutputLayer) .* (sigmoidGradient(Theta3 * Activation_HiddenLayer_2));
		Error_HiddenLayer_2 = ((Theta3(:,2:end))' * Error_HiddenLayer_3) .* (sigmoidGradient(Theta2 * Activation_HiddenLayer_1));
		Error_HiddenLayer_1 = ((Theta2(:,2:end))' * Error_HiddenLayer_2) .* (sigmoidGradient(Theta1 * Activation_InputLayer));

		Delta4_grad = Delta4_grad .+ (Error_OutputLayer * (Activation_HiddenLayer_3)');
		Delta3_grad = Delta3_grad .+ (Error_HiddenLayer_3 * (Activation_HiddenLayer_2)');
		Delta2_grad = Delta2_grad .+ (Error_HiddenLayer_2 * (Activation_HiddenLayer_1)');
		Delta1_grad = Delta1_grad .+ (Error_HiddenLayer_1 * (Activation_InputLayer)');
	end

	if (Hidden_Layer_Count == 2)
		Error_HiddenLayer_2 = ((Theta3(:,2:end))' * Error_OutputLayer) .* (sigmoidGradient(Theta2 * Activation_HiddenLayer_1));
		Error_HiddenLayer_1 = ((Theta2(:,2:end))' * Error_HiddenLayer_2) .* (sigmoidGradient(Theta1 * Activation_InputLayer));		
		
		Delta3_grad = Delta3_grad .+ (Error_OutputLayer * (Activation_HiddenLayer_2)');
		Delta2_grad = Delta2_grad .+ (Error_HiddenLayer_2 * (Activation_HiddenLayer_1)');
		Delta1_grad = Delta1_grad .+ (Error_HiddenLayer_1 * (Activation_InputLayer)');		
	end
	
	if (Hidden_Layer_Count == 1)
		Error_HiddenLayer_1 = ((Theta2(:,2:end))' * Error_OutputLayer) .* (sigmoidGradient(Theta1 * Activation_InputLayer));		
		
		Delta2_grad = Delta2_grad .+ (Error_OutputLayer * (Activation_HiddenLayer_1)');
		Delta1_grad = Delta1_grad .+ (Error_HiddenLayer_1 * (Activation_InputLayer)');		
	end	

	t=t+1;
end

%Calculate the gradient with regularization
%Also unroll the gradients

if (Hidden_Layer_Count == 5)	
	Theta6_grad=(Delta6_grad + (lambda .* ([zeros(size(Theta6,1),1) Theta6(:,2:end)]))) ./ (m);
	Theta5_grad=(Delta5_grad + (lambda .* ([zeros(size(Theta5,1),1) Theta5(:,2:end)]))) ./ (m);
	Theta4_grad=(Delta4_grad + (lambda .* ([zeros(size(Theta4,1),1) Theta4(:,2:end)]))) ./ (m);
	Theta3_grad=(Delta3_grad + (lambda .* ([zeros(size(Theta3,1),1) Theta3(:,2:end)]))) ./ (m);
	Theta2_grad=(Delta2_grad + (lambda .* ([zeros(size(Theta2,1),1) Theta2(:,2:end)]))) ./ (m);
	Theta1_grad=(Delta1_grad + (lambda .* ([zeros(size(Theta1,1),1) Theta1(:,2:end)]))) ./ (m);
	
	grad = [Theta1_grad(:) ; Theta2_grad(:) ; Theta3_grad(:) ; Theta4_grad(:) ; Theta5_grad(:) ; Theta6_grad(:)];	
end

if (Hidden_Layer_Count == 4)
	Theta5_grad=(Delta5_grad + (lambda .* ([zeros(size(Theta5,1),1) Theta5(:,2:end)]))) ./ (m);
	Theta4_grad=(Delta4_grad + (lambda .* ([zeros(size(Theta4,1),1) Theta4(:,2:end)]))) ./ (m);
	Theta3_grad=(Delta3_grad + (lambda .* ([zeros(size(Theta3,1),1) Theta3(:,2:end)]))) ./ (m);
	Theta2_grad=(Delta2_grad + (lambda .* ([zeros(size(Theta2,1),1) Theta2(:,2:end)]))) ./ (m);
	Theta1_grad=(Delta1_grad + (lambda .* ([zeros(size(Theta1,1),1) Theta1(:,2:end)]))) ./ (m);
	
	grad = [Theta1_grad(:) ; Theta2_grad(:) ; Theta3_grad(:) ; Theta4_grad(:) ; Theta5_grad(:)];		
end

if (Hidden_Layer_Count == 3)
	Theta4_grad=(Delta4_grad + (lambda .* ([zeros(size(Theta4,1),1) Theta4(:,2:end)]))) ./ (m);
	Theta3_grad=(Delta3_grad + (lambda .* ([zeros(size(Theta3,1),1) Theta3(:,2:end)]))) ./ (m);
	Theta2_grad=(Delta2_grad + (lambda .* ([zeros(size(Theta2,1),1) Theta2(:,2:end)]))) ./ (m);
	Theta1_grad=(Delta1_grad + (lambda .* ([zeros(size(Theta1,1),1) Theta1(:,2:end)]))) ./ (m);
	
	grad = [Theta1_grad(:) ; Theta2_grad(:) ; Theta3_grad(:) ; Theta4_grad(:)];			
end

if (Hidden_Layer_Count == 2)
	Theta3_grad=(Delta3_grad + (lambda .* ([zeros(size(Theta3,1),1) Theta3(:,2:end)]))) ./ (m);
	Theta2_grad=(Delta2_grad + (lambda .* ([zeros(size(Theta2,1),1) Theta2(:,2:end)]))) ./ (m);
	Theta1_grad=(Delta1_grad + (lambda .* ([zeros(size(Theta1,1),1) Theta1(:,2:end)]))) ./ (m);
	
	grad = [Theta1_grad(:) ; Theta2_grad(:) ; Theta3_grad(:)];	
end

if (Hidden_Layer_Count == 1)
	Theta2_grad=(Delta2_grad + (lambda .* ([zeros(size(Theta2,1),1) Theta2(:,2:end)]))) ./ (m);
	Theta1_grad=(Delta1_grad + (lambda .* ([zeros(size(Theta1,1),1) Theta1(:,2:end)]))) ./ (m);
	
	grad = [Theta1_grad(:) ; Theta2_grad(:)];	
end

% -------------------------------------------------------------

% =========================================================================


end
