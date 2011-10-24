#!/usr/bin/env jruby

class CsvRow
    def method_missing name, *args
        @row_data[@headers.index(name.to_s)]
    end
    def initialize(headers, row_data)
        @headers = headers
        @row_data = row_data
    end
end

module ActsAsCsv
    def self.included(base)
        base.extend ClassMethods
    end
    module ClassMethods
        def acts_as_csv
            include InstanceMethods
        end
    end
    module InstanceMethods
        def read
            @csv_contents = []
            filename = self.class.to_s.downcase + '.txt'
            file = File.new(filename)
            @headers = file.gets.chomp.split(', ')
            file.each do |row|
                @csv_contents << row.chomp.split(', ')
            end
        end
        def each
            @csv_contents.each do |r|
                yield CsvRow.new(@headers, r)
            end
        end
        attr_accessor :headers, :csv_contents
        def initialize
            read
        end
    end
end

class RubyCsv
    include ActsAsCsv
    acts_as_csv
end

m = RubyCsv.new
puts m.headers.inspect
puts m.csv_contents.inspect

m.each {|row| puts row.one}



