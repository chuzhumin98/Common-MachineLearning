clear all
%% 计算模型的置信区间
[error] = textread('compare.txt');
size(1) = 64620*19/20; %0.05的训练集
size(2) = 64620/2; %0.50的训练集
alpha = 0.1;
z = norminv(1-alpha/2,0,1);
CI = zeros(2,2);
for i = 1:2
    delta = z*sqrt(error(i)*(1-error(i))/size(i));
    CI(i,1) = error(i)-delta;
    CI(i,2) = error(i)+delta;
end
%% 进行模型之间的比较
sigma = sqrt(error(1)*(1-error(1))/size(1)+error(2)*(1-error(2))/size(2));
confidence1to2 = normcdf((error(2)-error(1))/sigma, 0, 1);
confidence2to1 = normcdf((error(1)-error(2))/sigma, 0, 1);