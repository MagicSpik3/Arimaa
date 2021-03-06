function h = plot_percentile_histogram(csvname, ymax)

% Read data for the percentile of each expert move from a .csv file 
% and create a histogram.

% M is a column vector of percentiles of all expert moves evaluated
M = csvread(csvname);

figure('Color',[1.0 1.0 1.0]); % color the figure background white instead of gray
nBins = 100; % the histogram has this many bars

[n,x] = hist(M, nBins);
bar(x, n./sum(n),1,'hist'); % normalize y axis to give proportions instead of counts

h = gca; % get a handle to the current axis
axis([0 1 0 ymax]) % gives x min/max and y min/max for axis scaling: [xmin xmax ymin ymax]


% modelname = strsplit(csvname, {'/', '_'}); % the file name is Folder/model_number -- we want model

title('Percentile rankings of expert moves', 'FontSize', 20);
xlabel('Percentile of expert move among all ordered moves', 'FontSize', 16);
ylabel('Proportion', 'FontSize', 16);
