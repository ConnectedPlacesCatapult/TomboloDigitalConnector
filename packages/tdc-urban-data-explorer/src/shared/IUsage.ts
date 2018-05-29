export interface IUsage {
  maps: number;
  datasets: number;
  totalStorage: number;
}

export interface IUsageReport {
 used: IUsage;
 limit: IUsage;
}
