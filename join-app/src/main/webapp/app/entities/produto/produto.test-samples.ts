import { IProduto, NewProduto } from './produto.model';

export const sampleWithRequiredData: IProduto = {
  id: 79836,
  nome: 'Extended end-to-end Aço',
  quantidade: 6106,
};

export const sampleWithPartialData: IProduto = {
  id: 87796,
  nome: 'SMTP Loan',
  quantidade: 8772,
};

export const sampleWithFullData: IProduto = {
  id: 86117,
  nome: 'Borders',
  quantidade: 26810,
};

export const sampleWithNewData: NewProduto = {
  nome: 'Rústico Licenciado',
  quantidade: 35096,
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
