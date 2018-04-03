clear all
[error] = textread('compare.txt');
errorMean(1) = mean(error(:,1)); %计算误差的平均值
errorMean(2) = mean(error(:,2));
size = length(error);
alpha = 0.05;
z = norminv(1-alpha/2,0,1);
CI = zeros(2,2);
for i = 1:2
    delta = z*sqrt(errorMean(i)*(1-errorMean(i))/size);
    CI(i,1) = errorMean(i)-delta;
    CI(i,2) = errorMean(i)+delta;
end