clear all
%% 计算模型的置信区间
[error] = textread('compare.txt');
errorMean(1) = mean(error(:,1)); %计算误差的平均值
errorMean(2) = mean(error(:,2));
size = length(error);
alpha = 0.01;
z = norminv(1-alpha/2,0,1);
CI = zeros(2,2);
for i = 1:2
    delta = z*sqrt(errorMean(i)*(1-errorMean(i))/size);
    CI(i,1) = errorMean(i)-delta;
    CI(i,2) = errorMean(i)+delta;
end
%% 进行模型之间的比较
sigma = sqrt(errorMean(1)*(1-errorMean(1))/size+errorMean(2)*(1-errorMean(2))/size);
confidence1to2 = normcdf((errorMean(2)-errorMean(1))/sigma, 0, 1);
confidence2to1 = normcdf((errorMean(1)-errorMean(2))/sigma, 0, 1);